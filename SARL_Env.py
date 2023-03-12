from JavalessClient import Client
from mazeCom import AwaitMoveMessageData, Errortype, MazeCom, MazeComMessagetype, MoveMessageData, BoardData
from Position import Position
from Card import Card
from NewBoard import Board

from gym import Env
from gym.spaces import Discrete, Box, Tuple, MultiBinary, MultiDiscrete, Dict
import numpy as np

import pydirectinput
import matplotlib.pyplot as plt

import functools
from pettingzoo import AECEnv, ParallelEnv
from pettingzoo.utils import agent_selector, parallel_to_aec, wrappers

import sys 
import numpy as np
import traceback
import operator

import time

HOST = "127.0.0.1"
PORT = 9571

class Agent_Client:
    def __init__(self, name, client: Client=None):
        self.name = name
        self.client = client


class MazeEnv(Env):

    # metadata = {"render.modes": ["human"], "normalize_images":False}
    def __init__(self, render_mode=None):
        super().__init__()
        self.action_space = MultiDiscrete([11, 4, 7, 7, 12], dtype=np.int8)
        self.observation_space = Dict({
                                    "player_pos": Box(low=0, high=6, shape=(2,), dtype=int),
                                    "next_treasure_pos": Box(low=0, high=6, shape=(2,), dtype=int)
                                })
        self.is_done = False
        self.agent = Agent_Client(name="player_1")
        self.render_mode = render_mode
        self.current_step = 0
        self.max_steps = 10000 # set the maximum number of steps
        

    def render(self):
        """
        Renders the environment. In human mode, it can print to terminal, open
        up a graphical window, or open up some other display that a human can see and understand.
        """
        pass

    def close(self):
        """
        Close should release any graphical displays, subprocesses, network connections
        or any other environment data which should not be kept around after the
        user is no longer using the environment.
        """
        pass

    def moeglicheOrientierungen(self, rot, card: Card):
        if(rot == 0):
            return Card(None, card.getShape(), Card.Orientation.D0.name, card.get_treasure())
        if(rot == 1):
            return Card(None, card.getShape(), Card.Orientation.D90.name, card.get_treasure())
        if(rot == 2):
            return Card(None, card.getShape(), Card.Orientation.D180.name, card.get_treasure())
        if(rot == 3):
            return Card(None, card.getShape(), Card.Orientation.D270.name, card.get_treasure())
        return Card(None, card.getShape(), Card.Orientation.D0.name, card.get_treasure())
    
    def Move(self, awaitMove: AwaitMoveMessageData, action):
        reward = 0
        boardData = awaitMove.get_board()
        treasure = awaitMove.get_treasureToFindNext()
        board = Board(boardData)
        playerPosition = board.findPlayer(self.agent.client.getId())
        potentialMove = MoveMessageData()
        psmp = Position.getPossiblePositionsForShiftcard()
        if board.get_forbidden() is None:
            psm = psmp[action[4]]
        else:
            for i in range(len(psmp)):
                if(psmp[i].equals(board.get_forbidden())):
                    del psmp[i]
                    break
            psm = psmp[action[0]]   
        potentialMove.set_shiftPosition(psm)
        treasurePositionData = board.findTreasure(treasure)
        
        orientedShiftCard = self.moeglicheOrientierungen(action[1], board.get_shiftCard())
        
        potentialMove.set_shiftCard(orientedShiftCard)
        potentialMove.set_newPinPos(playerPosition)

        boardNext = board.fakeShift(potentialMove)
#         if(boardNext.findPlayer(self.agent.client.getId()) is None):
#             boardNext = board.fakeShift(potentialMove)
#             print("board before shift:", board)
#             print("board after shift:", boardNext)
        treasurePositionData = Position(boardNext.findTreasure(treasure))
        new_player_pos = Position(boardNext.findPlayer(self.agent.client.getId()))
        reachablePositions = boardNext.getAllReachablePositions(new_player_pos)#
        rp = np.array(reachablePositions)
        chosen_pos_obj = Position(row=action[2], col=action[3])
        if(treasurePositionData == None):
            #print("no treasure? skipping")
            potentialMove.set_newPinPos(new_player_pos)
            self.next_treasure_pos = treasurePositionData.to_array()
            self.player_pos = new_player_pos.to_array()
            return potentialMove, reward
        is_in = False
        for i in range(len(rp)):
            if(chosen_pos_obj.equals(rp[i])):
                is_in = True
                break
            
        if(is_in == False):
            #print("cannot reach position, reward decreased")
            potentialMove.set_newPinPos(new_player_pos)
            self.next_treasure_pos = treasurePositionData.to_array()
            self.player_pos = new_player_pos.to_array()
            return potentialMove, reward - 3
        else:
            #print("treasure position data:", treasurePositionData)
            treasurePosition = Position(treasurePositionData)
            #print("treasure pos:", treasurePosition)

            if(chosen_pos_obj.equals(treasurePosition)):
                #print("straight to the treasure, reward increased!")
                potentialMove.set_newPinPos(treasurePosition)
                self.next_treasure_pos = treasurePositionData.to_array()
                self.player_pos = treasurePosition.to_array()
                return potentialMove, reward + 50
            else:
                #print("reachable position, but no treasure")
                potentialMove.set_newPinPos(chosen_pos_obj)
                self.next_treasure_pos = treasurePositionData.to_array()
                self.player_pos = chosen_pos_obj.to_array()
                return potentialMove, reward - 2

    
    def awaitMove(self, receivedMazeCom: MazeCom, action):
        awaitMove = receivedMazeCom.get_AwaitMoveMessage()
        move, reward = self.Move(awaitMove, action)
        #print("move over")
        mazeComToSend = MazeCom()
        mazeComToSend.set_id(self.agent.client.getId())
        mazeComToSend.set_messagetype(MazeComMessagetype.MOVE)
        mazeComToSend.set_MoveMessage(move)
        #print("move:", move)
        self.agent.client.out.write(mazeComToSend)
        return reward          
    
    def step(self, action):
        reward = 0
        try:
            receivedMazeCom = self.agent.client.in_.read_maze_com()
            if(receivedMazeCom.get_messagetype() == MazeComMessagetype.LOGINREPLY):
                id_ = receivedMazeCom.get_LoginReplyMessage().get_newID()
                self.agent.client.setId(id_)
            elif(receivedMazeCom.get_messagetype() == MazeComMessagetype.ACCEPT):
                if(receivedMazeCom.get_AcceptMessage().get_errortypeCode() != 'ILLEGAL_MOVE'):
                    reward += 2
            elif (receivedMazeCom.get_messagetype() == MazeComMessagetype.DISCONNECT):
                print(receivedMazeCom.get_DisconnectMessage())
                self.is_done = True
            elif(receivedMazeCom.get_messagetype() == MazeComMessagetype.AWAITMOVE):
                reward += self.awaitMove(receivedMazeCom, action)
            elif(receivedMazeCom.get_messagetype() == MazeComMessagetype.MOVEINFO):
                reward = reward
            elif(receivedMazeCom.get_messagetype() == MazeComMessagetype.WIN):
                print("You have won")
                self.is_done = True
                reward += 500
            else:
                print("Unknown message type: " + receivedMazeCom.get_messagetype())
        except Exception as e:
            print("exception time")
            print(traceback.format_exc())
            if str(type(e)) == "<java class 'java.net.SocketException'>":
                print("socket error, disconnected")
                self.is_done = True
                #sys.exit(1)
            elif str(type(e)) == "<java class 'java.io.EOFException'>":
                print("end reached, disconnecting")
                self.is_done = True
                #sys.exit(1)
        
        if not hasattr(self, 'start_time'):
            self.start_time = time.time()
        elapsed_time = time.time() - self.start_time
        # terminate the episode if the time limit is exceeded
        if elapsed_time >= 5 * 60:  # 5 minutes in seconds
            self.is_done = True

        self.current_step += 1
        if self.current_step >= self.max_steps:
            self.is_done = True
        
        observation = self._get_obs()
        done = self.is_done
        info = {}
        return observation, reward, done, False, info
    
    def reset(self):
        pydirectinput.click(x=1150, y=178)
        pydirectinput.click(x=1150, y=178)
        # start
        pydirectinput.click(x=1100, y=178)
        pydirectinput.click(x=1100, y=178)
        self.agent.client = Client(self.agent.name, HOST, PORT, False)
        try:
            login = self.agent.client.createLoginMessage(self.agent.name)
            self.agent.client.out.write(login)
            receivedMazeCom = self.agent.client.in_.read_maze_com()
            if(receivedMazeCom.get_messagetype() == MazeComMessagetype.LOGINREPLY):
                newid = receivedMazeCom.get_LoginReplyMessage().get_newID()
                self.agent.client.setId(newid)
            else:
                print(receivedMazeCom.get_AcceptMessage().get_errortypeCode())
        except Exception as e:
            print(traceback.format_exc())

        self.current_step = 0
        self.is_done = False
        self.player_pos = np.array([0, 0])
        self.next_treasure_pos = np.array([0, 0])
        observations = self._get_obs()
        self.start_time = time.time()
        return observations, {}


    def _get_obs(self):
        return {"player_pos": self.player_pos,
                "next_treasure_pos": self.next_treasure_pos}