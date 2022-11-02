#!/usr/bin/env python
# coding: utf-8


# Boiler plate stuff to start the module
import jpype
import jpype.imports
from jpype.types import *
import sys 
import numpy as np
import traceback
import random



# Launch the JVM
jpype.startJVM(classpath=['./maze-server-v2019.4.jar'])


# import the Java modules
from java.io import IOException
from java.net import Socket
from java.net import UnknownHostException

from javax.net.ssl import SSLSocket
from javax.net.ssl import SSLSocketFactory
from javax.xml.bind import JAXBException
from javax.xml.bind import UnmarshalException

from de.fhac.mazenet.server.generated import *

from de.fhac.mazenet.server.networking import MazeComMessageFactory
from de.fhac.mazenet.server.networking import XmlInputStream
from de.fhac.mazenet.server.networking import XmlOutputStream
from de.fhac.mazenet.server.game import Board
from de.fhac.mazenet.server.game import Position
from de.fhac.mazenet.server.game import Card
from java.lang import System
from java.lang import Integer


class Client:
    id_ = None
    def __init__(self, name, address, port, tls):
        if(tls):
            System.setProperty("javax.net.ssl.trustStore","clienttruststore.jks")
            System.setProperty("javax.net.ssl.trustStorePassword", "geheim")
            self.sslSocketFactory = SSLSocketFactory.getDefault()
            self.sslSocket = self.sslSocketFactory.createSocket(address, port)
            self.in_ = XmlInputStream(self.sslSocket.getInputStream())
            self.out = XmlOutputStream(self.sslSocket.getOutputStream())
        else:
            self.socket = Socket(address, port)
            self.in_ = XmlInputStream(self.socket.getInputStream())
            self.out = XmlOutputStream(self.socket.getOutputStream())

        self.name = name
        self.instance = None
        self.address = address
        self.port = port
        self.tls = tls
    
    def getId(self):
        return int(self.id_)

    def setId(self, id_):
        self.id_ = int(id_)

    def resetId(self):
        self.id_ = 0
    
    def run(self):
        print("ruun")
        login = MazeComMessageFactory.createLoginMessage(self.name)
        self.out.write(login)
        try:
            while(True):
                receivedMazeCom = self.in_.readMazeCom()
                if(receivedMazeCom.getMessagetype() == MazeComMessagetype.LOGINREPLY):
                    self.id_ = receivedMazeCom.getLoginReplyMessage().getNewID()
                elif(receivedMazeCom.getMessagetype() == MazeComMessagetype.ACCEPT):
                    print(receivedMazeCom.getAcceptMessage().getErrortypeCode())
                elif (receivedMazeCom.getMessagetype() == MazeComMessagetype.DISCONNECT):
                    print(receivedMazeCom.getDisconnectMessage().getErrortypeCode())
                    break
                elif(receivedMazeCom.getMessagetype() == MazeComMessagetype.AWAITMOVE):
                    self.awaitMove(receivedMazeCom)
                elif(receivedMazeCom.getMessagetype() == MazeComMessagetype.MOVEINFO):
                    print("in MoveInfo")
                elif(receivedMazeCom.getMessagetype() == MazeComMessagetype.WIN):
                    print("You have won")
                    break
                else:
                    print("Unknown message type: " + receivedMazeCom.getMessagetype())
        except Exception:
            print(traceback.format_exc())
    
    def createLoginManagerMessage(self, name):
        objectFactory = ObjectFactory()
        mazeCom = objectFactory.createMazeCom()
        mazeCom.setMessagetype(MazeComMessagetype.LOGIN)
        mazeCom.setId(-1)
        mazeCom.setLoginMessage(objectFactory.createLoginMessageData())
        mazeCom.getLoginMessage().setName(name)
        mazeCom.getLoginMessage().setRole(ClientRole.MANAGER)
        return mazeCom
    
    
    def login_manager(self):
        print("run manager")
        login = self.createLoginManagerMessage(self.name)
        self.out.write(login)
        receivedMazeCom = self.in_.readMazeCom()
        if(receivedMazeCom.getMessagetype() == MazeComMessagetype.LOGINREPLY):
            self.id_ = receivedMazeCom.getLoginReplyMessage().getNewID()

    def start_game(self, num_players):
        msg = MazeCom()
        msg.setMessagetype(MazeComMessagetype.CONTROLSERVER)
        control_server_data = ControlServerData()
        control_server_data.setPlayerCount(Integer(num_players))
        control_server_data.setCommand("START")
        msg.setControlServerMessage(control_server_data)
        self.out.write(msg)

    def stop_game(self, num_players):
        msg = MazeCom()
        msg.setMessagetype(MazeComMessagetype.CONTROLSERVER)
        control_server_data = ControlServerData()
        control_server_data.setPlayerCount(Integer(num_players))
        control_server_data.setCommand("STOP")
        msg.setControlServerMessage(control_server_data)
        self.out.write(msg)
        # try:
        #     while(True):
                
        #         if(receivedMazeCom.getMessagetype() == MazeComMessagetype.ACCEPT):
        #             print(receivedMazeCom.getAcceptMessage().getErrortypeCode())
        #         elif (receivedMazeCom.getMessagetype() == MazeComMessagetype.DISCONNECT):
        #             print(receivedMazeCom.getDisconnectMessage().getErrortypeCode())
        #             break
        #         elif(receivedMazeCom.getMessagetype() == MazeComMessagetype.AWAITMOVE):
        #             self.awaitMove(receivedMazeCom)
        #         elif(receivedMazeCom.getMessagetype() == MazeComMessagetype.MOVEINFO):
        #             print("in MoveInfo")
        #         elif(receivedMazeCom.getMessagetype() == MazeComMessagetype.WIN):
        #             print("You have won")
        #             break
        #         else:
        #             print("Unknown message type: " + receivedMazeCom.getMessagetype())
        # except Exception:
        #     print(traceback.format_exc())

    def awaitMove(self, mazeCom):
        board = Board(mazeCom.getAwaitMoveMessage().getBoard())
        oldPlayerPosition = board.findPlayer(self.id_)

        possiblePositionsForShiftcard = Position.getPossiblePositionsForShiftcard()
        forbiddenData = board.getForbidden()
        forbidden = None
        if (forbiddenData != None):
            forbidden = Position(forbiddenData)
        try:
            possiblePositionsForShiftcard.remove(forbidden)
        except:
            print("continue")
        randomShiftCardPosition = random.choice(possiblePositionsForShiftcard)

        shiftCard = Card(board.getShiftCard())
        randomShiftCard = random.choice(shiftCard.getPossibleRotations())

        objectFactory = ObjectFactory()
        moveMessageData = objectFactory.createMoveMessageData()
        moveMessageData.setShiftPosition(randomShiftCardPosition)
        moveMessageData.setShiftCard(randomShiftCard)
        moveMessageData.setNewPinPos(oldPlayerPosition)
        newBoard = board.fakeShift(moveMessageData)

        newPlayerPosition = newBoard.findPlayer(self.id_)
        randomPlayerPosition = random.choice(newBoard.getAllReachablePositions(newPlayerPosition))
        moveMessageData.setNewPinPos(randomPlayerPosition)

        mazeComToSend = objectFactory.createMazeCom()
        mazeComToSend.setMoveMessage(moveMessageData)
        mazeComToSend.setMessagetype(MazeComMessagetype.MOVE)
        mazeComToSend.setId(self.id_)
        self.out.write(mazeComToSend)
    