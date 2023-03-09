from JavalessClient import Client
from mazeCom import AwaitMoveMessageData, Errortype, MazeCom, MazeComMessagetype, MoveMessageData, BoardData
from Position import Position
from Card import Card
from NewBoard import Board

from gymnasium.spaces import Discrete, Box, Tuple, MultiBinary, MultiDiscrete, Dict
import numpy as np

import pydirectinput
import matplotlib.pyplot as plt

import functools
from pettingzoo import AECEnv, ParallelEnv
from pettingzoo.utils import agent_selector,  wrappers
from pettingzoo.utils.conversions import parallel_wrapper_fn, parallel_to_aec_wrapper, aec_to_parallel, parallel_to_aec

import sys 
import numpy as np
import traceback
import operator

HOST = "127.0.0.1"
PORT = 9571

def env(render_mode=None, args=None):
    env = MARL_Env_Parallel()
    if render_mode == "ansi":
        env = wrappers.CaptureStdoutWrapper(env)
    # env = wrappers.TerminateIllegalWrapper(env, illegal_reward=-1)
    # env = wrappers.AssertOutOfBoundsWrapper(env)
    #env = wrappers.OrderEnforcingWrapper(env)
    #env = wrappers.BaseParallelWraper(env=env)
    env = parallel_to_aec(env)
    #env = aec_to_parallel(env)
    return env

#parallel_env = parallel_wrapper_fn(env)


class Agent_Client:
    def __init__(self, name, client: Client=None):
        self.name = name
        self.client = client


class MARL_Env_Parallel(ParallelEnv):
    metadata = {"render_modes": ["human"], "name": "marl", "is_parallelizable": True}
    def __init__(self, render_mode=None, num_players=4):
        super().__init__()
        self.num_players = num_players
        self.possible_agents = [Agent_Client(name="player_" + str(r)) for r in range(self.num_players)]
        self.agent_name_mapping = dict(
            zip(self.possible_agents, list(range(len(self.possible_agents))))
        )
        self._action_spaces = {agent: MultiDiscrete([11, 4, 7, 7, 12], dtype=np.int8) for agent in self.possible_agents}
        #self._observation_spaces = #{"observation",{agent: Box(low=0, high=6, shape=(4,), dtype=np.uint8) for agent in self.possible_agents},
                                   # "mask"}
        self._observation_spaces = {agent: Dict({   
                                            "observation": Box(low=0, high=1, shape=(3, 3, 2), dtype=np.int8),
                                            "action_mask": Box(low=0, high=1, shape=(9,), dtype=np.int8),
                                            })for agent in self.possible_agents
                                    }
        self.render_mode = render_mode
        self.player_pos = {}
        self.next_treasure_pos = {}
        self.is_done = False
        

    # this cache ensures that same space object is returned for the same agent
    # allows action space seeding to work as expected
    @functools.lru_cache(maxsize=None)
    def observation_space(self, agent):
        # gymnasium spaces are defined and documented here: https://gymnasium.farama.org/api/spaces/
        return self._observation_spaces[agent]
    
    @functools.lru_cache(maxsize=None)
    def action_space(self, agent):
        return self._action_spaces[agent]

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
        card_ = Card(c=card)
        openings = card_.get_openings()
#         if(card.getShape() == Card.CardShape.I):
#             openings.set_bottom(not openings.get_bottom())
#             openings.set_left(not openings.get_left())
#             openings.set_right(not openings.get_right())
#             openings.set_top(not openings.get_top())
#             return  Card(c=card_)
#         else:
        if(rot == 0):
            return Card(None, card.getShape(), Card.Orientation.D0.name, card.get_treasure())
        if(rot == 1):
            return Card(None, card.getShape(), Card.Orientation.D90.name, card.get_treasure())
        if(rot == 2):
            return Card(None, card.getShape(), Card.Orientation.D180.name, card.get_treasure())
        if(rot == 3):
            return Card(None, card.getShape(), Card.Orientation.D270.name, card.get_treasure())
        return Card(None, card.getShape(), Card.Orientation.D0.name, card.get_treasure())
    
    def Move(self, awaitMove: AwaitMoveMessageData, action, agent):
        reward = 0
        boardData = awaitMove.get_board()
        treasure = awaitMove.get_treasureToFindNext()
        board = Board(boardData)
        playerPosition = board.findPlayer(agent.client.getId())
        potentialMove = MoveMessageData()
        psmp = Position.getPossiblePositionsForShiftcard()#copy.copy(potentialShiftMovesPos)
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
#         if(boardNext.findPlayer(agent.client.getId()) is None):
#             boardNext = board.fakeShift(potentialMove)
#             print("board before shift:", board)
#             print("board after shift:", boardNext)
        treasurePositionData = boardNext.findTreasure(treasure)
        new_player_pos = boardNext.findPlayer(agent.client.getId())
        reachablePositions = boardNext.getAllReachablePositions(new_player_pos)#
        rp = np.array(reachablePositions)
        chosen_pos_obj = Position(row=action[2], col=action[3])
        chosen_pos_arr = np.array(action[2], action[3])
        if(treasurePositionData == None):
            print("no treasure? skipping")
            potentialMove.set_newPinPos(new_player_pos)
            self.next_treasure_pos[agent] = treasurePositionData
            self.player_pos[agent] = new_player_pos
            return potentialMove, reward
        is_in = False
        for i in range(len(rp)):
            if(chosen_pos_obj.equals(rp[i])):
                is_in = True
                break
            
        if(is_in == False):
            print("cannot reach position, reward decreased")
            potentialMove.set_newPinPos(new_player_pos)
            self.next_treasure_pos[agent] = treasurePositionData
            self.player_pos[agent] = new_player_pos
            return potentialMove, reward - 10
        else:
            print("treasure position data:", treasurePositionData)
            treasurePosition = Position(treasurePositionData)
            print("treasure pos:", treasurePosition)

            if(chosen_pos_obj.equals(treasurePosition)):
                print("straight to the treasure, reward increased!")
                potentialMove.set_newPinPos(treasurePosition)
                self.next_treasure_pos[agent] = treasurePositionData
                self.player_pos[agent] = treasurePosition
                return potentialMove, reward + 20
            else:
                print("reachable position, but no treasure")
                potentialMove.set_newPinPos(chosen_pos_obj)
                self.next_treasure_pos[agent] = treasurePositionData
                self.player_pos[agent] = chosen_pos_obj
                return potentialMove, reward - 5

        print("Object not reachable: random move :/")
        potentialMove.setNewPinPos(reachablePositions[0])
        self.next_treasure_pos[agent] = treasurePositionData
        self.player_pos[agent] = reachablePositions[0]
        return potentialMove, reward - 10
    
    def awaitMove(self, receivedMazeCom: MazeCom, action, agent):
        awaitMove = receivedMazeCom.get_AwaitMoveMessage()
        move, reward = self.Move(awaitMove, action, agent)
        print("move over")
        mazeComToSend = MazeCom()
        mazeComToSend.set_id(agent.client.getId())
        mazeComToSend.set_messagetype(MazeComMessagetype.MOVE)
        mazeComToSend.set_MoveMessage(move)
        print("move:", move)
        agent.client.out.write(mazeComToSend)
        return reward          
    
    def run_agent(self, agent, action, return_val):
        reward = 0
        #print(f"agents:", agent)
        #print(f"actions:", action)
        try:
#             print("starrt rer")
            receivedMazeCom = agent.client.in_.read_maze_com()
            
#             print("end ererer")
            if(receivedMazeCom.get_messagetype() == MazeComMessagetype.LOGINREPLY):
                id_ = receivedMazeCom.get_LoginReplyMessage().get_newID()
                agent.client.setId(id_)
            elif(receivedMazeCom.get_messagetype() == MazeComMessagetype.ACCEPT):
                
                print("msg:", receivedMazeCom.get_AcceptMessage().get_errortypeCode())
                
                if(receivedMazeCom.get_AcceptMessage().get_errortypeCode() == 'ILLEGAL_MOVE'):
                    global last_move
                    #print("last_move:", last_move)
                else:
                    reward += 2
            elif (receivedMazeCom.get_messagetype() == MazeComMessagetype.DISCONNECT):
                print(receivedMazeCom.get_DisconnectMessage())
                #reward -= 10
            elif(receivedMazeCom.get_messagetype() == MazeComMessagetype.AWAITMOVE):
                reward += self.awaitMove(receivedMazeCom, action, agent)
            elif(receivedMazeCom.get_messagetype() == MazeComMessagetype.MOVEINFO):
                print("in MoveInfo")
            elif(receivedMazeCom.get_messagetype() == MazeComMessagetype.WIN):
                print("You have won")
                self.is_done = True
                reward += 100
            else:
                print("Unknown message type: " + receivedMazeCom.getMessagetype())
        except Exception as e:
            print("exception time")
            print(traceback.format_exc())
            if str(type(e)) == "<java class 'java.net.SocketException'>":
                print("socket error, disconnected")
                sys.exit(1)
            elif str(type(e)) == "<java class 'java.io.EOFException'>":
                print("end reached, disconnecting")
                sys.exit(1)
        #print("self.agents:", self.agents)
#         print("self.player_pos:", self.player_pos)
#         print("self.next_treasure_pos:", self.next_treasure_pos)
        try:
            observations = list(self.player_pos[agent], self.next_treasure_pos[agent])
        except:
            observations = (None, None)
        
        if self.is_done:
            terminations = True
        else:
            terminations = False
        truncations = False
        infos = {}
        return_val[0] = observations
        return_val[1] = reward
        return_val[2] = terminations
        return_val[3] = truncations
        return_val[4] = infos
    
    def reset(self, seed=None, return_info=False, options=None):
        """
        Reset needs to initialize the `agents` attribute and must set up the
        environment so that render(), and step() can be called without issues.
        Here it initializes the `num_moves` variable which counts the number of
        hands that are played.
        Returns the observations for each agent
        """
        try:
            pydirectinput.click(x=1150, y=178)
            pydirectinput.click(x=1150, y=178)
            # start
            pydirectinput.click(x=1100, y=178)
            pydirectinput.click(x=1100, y=178)
            for agent in self.possible_agents:
                agent.client = Client(agent.name, HOST, PORT, False)
                try:
                    login = agent.client.createLoginMessage(agent.name)
                    agent.client.out.write(login)
                    receivedMazeCom = agent.client.in_.read_maze_com()
                    if(receivedMazeCom.get_messagetype() == MazeComMessagetype.LOGINREPLY):
                        newid = receivedMazeCom.get_LoginReplyMessage().get_newID()
                        agent.client.setId(newid)
                    else:
                        print(receivedMazeCom.get_AcceptMessage().get_errortypeCode())
                except Exception as e:
                    print(traceback.format_exc())

            self.is_done = False
            self.possible_agents = sorted(self.possible_agents, key=operator.attrgetter('client.id_'))
            self.agents = self.possible_agents[:]
            observations = {agent: None for agent in self.agents}
            self.terminations = {agent: False for agent in self.agents}
            self.truncations = {agent: False for agent in self.agents}
            print("reset done")
            if not return_info:
                return observations
            else:
                infos = {agent: {} for agent in self.agents}
                return observations, infos
        except:
            pass

    def step(self, actions):
        """
        step(action) takes in an action for each agent and should return the
        - observations
        - rewards
        - terminations
        - truncations
        - infos
        dicts where each dict looks like {agent_1: item_1, agent_2: item_2}
        """
        # If a user passes in actions with no agents, then just return empty observations, etc.
        if not actions:
            self.agents = []
            return {}, {}, {}, {}, {}
        
        i = 0
        return_val= [[None]*5]*self.num_players
        for agent in self.agents:
            try:
                self.run_agent(agent, actions[agent], return_val[i])
                i += 1
            except:
                print(traceback.format_exc())
        
        observations, rewards, terminations, truncations, infos = {}, {}, {}, {}, {}
        
        for t in range(self.num_players):
            observations[self.agents[t]] = return_val[t][0]
            rewards[self.agents[t]] = return_val[t][1]
            terminations[self.agents[t]] = return_val[t][2]
            truncations[self.agents[t]] = return_val[t][3]
            infos[self.agents[t]] = return_val[t][4]
        print("we're done")
        # rewards for all agents are placed in the rewards dictionary to be returned
        return observations, rewards, terminations, truncations, infos
    
    # def observe(self, agent):
    #     """
    #     Observe should return the observation of the specified agent. This function
    #     should return a sane observation (though not necessarily the most up to date possible)
    #     at any time after reset() is called.
    #     """
    #     # observation of one agent is the previous state of the other
    #     return np.array(self.observations[agent])