#!/usr/bin/env python
# coding: utf-8


# Boiler plate stuff to start the module
import sys 
import numpy as np
import traceback
import random



from XmlOutputStream import XmlOutputStream
from XmlInputStream import XmlInputStream
#from de.fhac.mazenet.server.networking import XmlOutputStream
# from de.fhac.mazenet.server.generated import AwaitMoveMessageData
# from de.fhac.mazenet.server.generated import Errortype
# from de.fhac.mazenet.server.generated import MazeCom
# from de.fhac.mazenet.server.generated import MazeComMessagetype
# #from de.fhac.mazenet.server.generated import MoveMessageDatas
# from de.fhac.mazenet.server.generated import BoardData
from mazeCom import ObjectFactory, ClientRole, ControlServerData

import socket
#from de.fhac.mazenet.server.game import Board
#from de.fhac.mazenet.server.game import Position
#from de.fhac.mazenet.server.game import Card
from mazeCom import AwaitMoveMessageData, Errortype, MazeCom
from mazeCom import MazeComMessagetype
from mazeCom import MoveMessageData, BoardData
#from Boardpy import Board
#from MoveMessageDatapy import MoveMessageData
from Position import Position
from Card import Card
from NewBoard import Board
import ssl

class Client:
    id_ = None
    def __init__(self, name, address, port, tls):
        if(tls):
            # Set the path to the truststore file and its password
            truststore_path = "clienttruststore.jks"
            truststore_password = "geheim"

            # # Create an SSL context and set the truststore properties
            # ssl_context = ssl.create_default_context()
            # ssl_context.load_verify_locations(cafile=truststore_path)
            # ssl_context.check_hostname = False
            # ssl_context.verify_mode = ssl.CERT_NONE

            # # Create an SSL socket using the SSL context
            # ssl_socket = ssl_context.wrap_socket(socket.socket(socket.AF_INET, socket.SOCK_STREAM))
            # # System.setProperty("javax.net.ssl.trustStore","clienttruststore.jks")
            # # System.setProperty("javax.net.ssl.trustStorePassword", "geheim")
            # # self.sslSocketFactory = SSLSocketFactory.getDefault()
            # self.sslSocket = self.sslSocketFactory.createSocket(address, port)

            # Load the SSL context from a .jks file
            # context = ssl.create_default_context(ssl.PROTOCOL_TLS_CLIENT)
            #context.set_log_level(ssl.LOG_LEVEL_DEBUG)

            # context = ssl.SSLContext(ssl.PROTOCOL_TLSv1_2)
            # context.options |= ssl.OP_NO_TLSv1 | ssl.OP_NO_TLSv1_1 | ssl.OP_NO_SSLv2 | ssl.OP_NO_SSLv3

            #context.load_cert_chain(certfile="mycert.pem", keyfile="clientkey.pem", password="geheim")
            # context = ssl.SSLContext(ssl.PROTOCOL_TLS_CLIENT)
            # context.load_verify_locations(truststore_path)
            
            s = socket.socket(socket.AF_INET)
            conn = ssl.wrap_socket(s, ca_certs="mycert.pem")

            conn.connect((address, port))

            # # Create a TCP socket
            # sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            # #sock.settimeout(10)

            # # Wrap the TCP socket with SSL
            # s = context.wrap_socket(sock, server_hostname=address)

            # # Connect to the server
            # s.connect((address, port))

            self.in_ = XmlInputStream(conn)
            self.out = XmlOutputStream(conn)
        else:
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.connect((address, port))
            self.in_ = XmlInputStream(s)
            self.out = XmlOutputStream(s)
            

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
    
    def createLoginMessage(self, name):
        objectFactory = ObjectFactory()
        mazeCom = objectFactory.createMazeCom()
        mazeCom.set_messagetype(MazeComMessagetype.LOGIN)
        mazeCom.set_id(-1)
        mazeCom.set_LoginMessage(objectFactory.createLoginMessageData())
        mazeCom.get_LoginMessage().set_name(name)
        mazeCom.get_LoginMessage().set_role(ClientRole.PLAYER)
        return mazeCom
    

    def run(self):
        print("ruun")
        login = self.createLoginMessage(self.name)
        self.out.write(login)
        print("run 2")
        try:
            while(True):
                
                receivedMazeCom = self.in_.read_maze_com()
                print("run 3")
                if(receivedMazeCom.get_messagetype() == MazeComMessagetype.LOGINREPLY):
                    self.id_ = receivedMazeCom.get_LoginReplyMessage().get_newID()
                elif(receivedMazeCom.get_messagetype() == MazeComMessagetype.ACCEPT):
                    print("msg:", receivedMazeCom.get_AcceptMessage().get_errortypeCode())
                    # if(receivedMazeCom.get_AcceptMessage().get_accept() == 'false'):
                    #     self.out.write(login)
                elif (receivedMazeCom.get_messagetype() == MazeComMessagetype.DISCONNECT):
                    print(receivedMazeCom.get_DisconnectMessage())
                    break
                elif(receivedMazeCom.get_messagetype() == MazeComMessagetype.AWAITMOVE):
                    self.awaitMove(receivedMazeCom)
                elif(receivedMazeCom.get_messagetype() == MazeComMessagetype.MOVEINFO):
                    print("in MoveInfo")
                elif(receivedMazeCom.get_messagetype() == MazeComMessagetype.WIN):
                    print("You have won")
                    break
                else:
                    print("Unknown message type: " + receivedMazeCom.get_messagetype())
                print("run 4")
        except Exception:
            print(traceback.format_exc())
    
    def createLoginManagerMessage(self, name):
        objectFactory = ObjectFactory()
        mazeCom = objectFactory.createMazeCom()
        mazeCom.set_messagetype(MazeComMessagetype.LOGIN)
        mazeCom.set_id(-1)
        mazeCom.set_LoginMessage(objectFactory.createLoginMessageData())
        mazeCom.get_LoginMessage().set_name(name)
        mazeCom.get_LoginMessage().set_role(ClientRole.MANAGER)
        return mazeCom
      
    def login_manager(self):
        print("run manager")
        login = self.createLoginManagerMessage(self.name)
        self.out.write(login)
        receivedMazeCom = self.in_.read_maze_com()
        if(receivedMazeCom.get_messagetype() == MazeComMessagetype.LOGINREPLY):
            self.id_ = receivedMazeCom.get_LoginReplyMessage().get_newID()

    def start_game(self, num_players):
        msg = MazeCom()
        msg.set_messagetype(MazeComMessagetype.CONTROLSERVER)
        control_server_data = ControlServerData()
        control_server_data.set_playerCount(num_players)
        control_server_data.set_command("START")
        msg.set_id(self.id_)
        msg.set_ControlServerMessage(control_server_data)
        self.out.write(msg)

    def stop_game(self, num_players):
        msg = MazeCom()
        msg.set_messagetype(MazeComMessagetype.CONTROLSERVER)
        control_server_data = ControlServerData()
        control_server_data.set_playerCount(num_players)
        control_server_data.set_command("STOP")
        msg.set_id(self.id_)
        msg.set_ControlServerMessage(control_server_data)
        self.out.write(msg)


    def awaitMove(self, mazeCom: MazeCom):
        board = Board(mazeCom.get_AwaitMoveMessage().get_board())
        oldPlayerPosition = board.findPlayer(self.id_)

        possiblePositionsForShiftcard = Position.getPossiblePositionsForShiftcard()
        forbiddenData = board.get_forbidden()
        forbidden = None
        if (forbiddenData != None):
            forbidden = Position(forbiddenData)
        try:
            possiblePositionsForShiftcard.remove(forbidden)
        except:
            print("continue")
        print("test 1")
        #print("poss_pos_shft:", possiblePositionsForShiftcard)
        randomShiftCardPosition = possiblePositionsForShiftcard[0]
        shiftCard = Card(board.get_shiftCard())
        randomShiftCard = shiftCard.getPossibleRotations()[0]
        print("test 2")
        objectFactory = ObjectFactory()
        moveMessageData = objectFactory.createMoveMessageData()
        moveMessageData.set_shiftPosition(randomShiftCardPosition)
        moveMessageData.set_shiftCard(randomShiftCard)
        moveMessageData.set_newPinPos(oldPlayerPosition)
        newBoard = board.fakeShift(moveMessageData)

        print("test 3")
        print("id:", self.id_)
        newPlayerPosition = newBoard.findPlayer(self.id_)
        print("in t3 1")
        print("all reach pos:", newBoard.getAllReachablePositions(newPlayerPosition))
        randomPlayerPosition = newBoard.getAllReachablePositions(newPlayerPosition)[0]
        print("in t3 2")
        moveMessageData.set_newPinPos(randomPlayerPosition)
        print("test 4")
        mazeComToSend = objectFactory.createMazeCom()
        mazeComToSend.set_MoveMessage(moveMessageData)
        mazeComToSend.set_messagetype(MazeComMessagetype.MOVE)
        mazeComToSend.set_id(self.id_)
        self.out.write(mazeComToSend)
    