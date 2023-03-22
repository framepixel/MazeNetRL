import io
from xml.etree.ElementTree import fromstring
from xml.etree.ElementTree import ParseError
from typing import Any
import tempfile
import os
from UTFInputStream import UTFInputStream
from pkg_resources import resource_stream
import xml.etree.ElementTree as ET
from mazeCom import MazeCom, MazeComMessagetype, AcceptMessageData, MoveMessageData, MoveInfoData
from mazeCom import LoginMessageData, LoginReplyMessageData, AwaitMoveMessageData, GameStatusData
from mazeCom import ControlServerData, WinMessageData, DisconnectMessageData, TreasuresToGoData
from mazeCom import BoardData, Row, DisconnectMessageData
from Position import Position
from Card import Card

# class XmlInputStream():
#     def __init__(self, input_stream):
#         #super().__init__(input_stream)
#         self.input_stream = input_stream
#         try:
#             with open('./mazeCom.xsd', 'r') as f:
#                 xml_string = f.read()
#             self.unmarshaller = fromstring(xml_string)
#         except ParseError as e:
#             print("err cons", e)

#     def read_maze_com(self) -> Any:
#         # try:
#         #     # Convert the bytes to a string
#         #     xml_data = self.input_stream.recv(4096)
#         #     xml_string = xml_data.decode("UTF-8", errors='ignore')
#         #     # Parse the XML string into an ElementTree
#         #     root = ET.fromstring(xml_string)
#         #     # Do something with the ElementTree, like extracting data or converting it to a custom object
#         #     # ...
#         #     result = self.xml_to_maze_com(root)
#         #     return result
#         # except ET.ParseError as e:
#         #     print("Error parsing XML data:", e)

#         data = b''
#         while not data.endswith(b'</MazeCom>'):
#             data += self.input_stream.recv(4096)
#         xml_data = data.decode("UTF-8")
#         if not xml_data.startswith("<?xml"):
#             print("Error parsing XML data: not well-formed (invalid token): line 1, column 0")
#             return None
#         return self.xml_to_maze_com(xml_data)


#     def xml_to_maze_com(self, root: str) -> Any:
#         #root = ET.fromstring(xml)
#         messagetype_str = root.attrib['messagetype']
#         id_str = root.attrib['id']
#         maze_com = MazeCom(messagetype=MazeComMessagetype[messagetype_str], id=int(id_str))
#         for child in root:
#             if child.tag == "LoginMessage":
#                 maze_com.set_LoginMessage(child.text)
#             elif child.tag == "LoginReplyMessage":
#                 maze_com.set_LoginReplyMessage(child.text)
#             elif child.tag == "AwaitMoveMessage":
#                 maze_com.set_AwaitMoveMessage(child.text)
#             elif child.tag == "MoveMessage":
#                 maze_com.set_MoveMessage(child.text)
#             elif child.tag == "MoveInfoMessage":
#                 maze_com.set_MoveInfoMessage(child.text)
#             elif child.tag == "GameStatusMessage":
#                 maze_com.set_GameStatusMessage(child.text)
#             elif child.tag == "ControlServerMessage":
#                 maze_com.set_ControlServerMessage(child.text)
#             elif child.tag == "AcceptMessage":
#                 maze_com.set_AcceptMessage(child.text)
#             elif child.tag == "WinMessage":
#                 maze_com.set_WinMessage(child.text)
#             elif child.tag == "DisconnectMessage":
#                 maze_com.set_DisconnectMessage(child.text)
#             # Add more elif blocks for other child elements as needed
#         print("mazeccoecm: ", maze_com)
#         return maze_com


import xml.sax
from mazeCom import Row, Openings, Pin, Treasure, CardData
from NewBoard import Board
# class MazeComHandler(xml.sax.ContentHandler):
#     def __init__(self):
#         self.maze_com = None
#         self.current_tag = None
#         self.text = ""

#     def startElement(self, name, attrs):
#         self.current_tag = name
#         if name == "MazeCom":
#             self.messagetype_str = attrs.getValue("messagetype")
#             self.id_str = attrs.getValue("id")
#             self.maze_com = MazeCom(messagetype=MazeComMessagetype[self.messagetype_str], id=int(self.id_str))
#         if name == "AcceptMessage":  # added
#             self.accept_message = AcceptMessageData()
#         if name == "board":
#             self.current_board = Board()
#         if name == "row":
#             self.current_row = Row()
#         if name == "col":
#             self.current_col = CardData()
#         if name == "openings":
#             self.current_openings = Openings()
#         if name == "pin":
#             self.current_pin = Pin()
#         if name == "treasure":
#             self.current_treasure = Treasure()
#         if name == "shiftCard":
#             self.current_shiftCard = CardData()
#         if name == 'treasureToGo':
#             self.current_treasure_to_go = TreasuresToGoData()

#     def characters(self, content):
#         self.text = content.strip()
#         if self.current_tag == "accept":  # added
#             self.accept = self.text
#         if self.current_tag == "errortypeCode":  # added
#             self.errortypeCode = self.text
#         if self.current_tag == "top":
#             self.current_openings.top = self.text
#         elif self.current_tag == "bottom":
#             self.current_openings.bottom = self.text
#         elif self.current_tag == "left":
#             self.current_openings.left = self.text
#         elif self.current_tag == "right":
#             self.current_openings.right = self.text
#         elif self.current_tag == "playerID":
#             self.current_pin.playerID = self.text
#         elif self.current_tag == "treasure":
#             self.current_treasure.treasure = self.text
#         elif self.current_tag == "player":
#             self.current_treasure_to_go.set_player(self.text)
#         elif self.current_tag == "treasures":
#             self.current_treasure_to_go.set_treasures(self.text)
            

#     def endElement(self, name):
#         if name == "LoginMessage":
#             login = LoginMessageData()
#             self.maze_com.set_LoginMessage(login)
#         elif name == "LoginReplyMessage":
#             login = LoginReplyMessageData()
#             self.maze_com.set_LoginReplyMessage(login)
#         elif name == "MoveMessage":
#             move = MoveMessageData()
#             self.maze_com.set_MoveMessage(move)
#         elif name == "MoveInfoMessage":
#             move = MoveInfoData()
#             self.maze_com.set_MoveInfoMessage(move)
#         elif name == "GameStatusMessage":
#             game = GameStatusData()
#             self.maze_com.set_GameStatusMessage(game)
#         elif name == "ControlServerMessage":
#             control = ControlServerData()
#             self.maze_com.set_ControlServerMessage(control)
#         elif name == "AcceptMessage":
#             print("self.text:", self.text)
#             self.accept_message.set_accept(self.accept)
#             self.accept_message.set_errortypeCode(self.errortypeCode)
#             self.maze_com.set_AcceptMessage(self.accept_message)
#         elif name == "WinMessage":
#             win = WinMessageData()
#             self.maze_com.set_WinMessage(win)
#         elif name == "DisconnectMessage":
#             disc = DisconnectMessageData()
#             self.maze_com.set_DisconnectMessage(disc)
#         elif name == "AwaitMoveMessage":
#             await_move = AwaitMoveMessageData()
#             await_move.set_board(self.current_board)
#             self.maze_com.set_AwaitMoveMessage(await_move)
#             self.current_board = None
#         elif name == "name":
#             self.maze_com.get_LoginMessage().set_name(self.text)
#         elif name == "role":
#             self.maze_com.get_LoginMessage().set_role(self.text)
#         elif name == "board":
#             self.board = Board()
#             self.maze_com.get_AwaitMoveMessage().set_board(self.board)
#         elif name == "row":
#             self.row = Row()
#             self.board.add_row(self.row)
#         elif name == "col":
#             self.col = CardData()
#             self.row.add_col(self.col)
#         elif name == "openings":
#             self.openings = Openings()
#             self.col.set_openings(self.openings)
#         elif name == "top":
#             self.openings.set_top(self.text)
#         elif name == "bottom":
#             self.openings.set_bottom(self.text)
#         elif name == "left":
#             self.openings.set_left(self.text)
#         elif name == "right":
#             self.openings.set_right(self.text)
#         elif name == "pin":
#             self.pin = Pin()
#             self.col.set_pin(self.pin)
#         elif name == "playerID":
#             self.pin.set_playerID(self.text)
#         elif name == "treasure":
#             self.col.set_treasure(self.text)
#         elif name == "shiftCard":
#             self.shiftCard = CardData()
#             self.maze_com.get_AwaitMoveMessage().set_shiftCard(self.shiftCard)
#         elif name == "openings":
#             self.shiftCardOpenings = Openings()
#             self.shiftCard.set_openings(self.openings)
#         elif name == "player":
#             self.current_treasure_to_go.set_player(self.text)
#         elif name == "treasures":
#             self.current_treasure_to_go.set_treasures(self.text)

    
#         self.current_tag = None
#         self.text = ""

from lxml import etree
class XmlInputStream():
    def __init__(self, input_stream):
        #super().__init__(input_stream)
        self.input_stream = input_stream

    def read_maze_com(self) -> MazeCom:
        data = b''
        while not data.endswith(b'</MazeCom>\n'):
            data += self.input_stream.recv(4096)
            #print("data:", data)
        data = data[4:]
        #print("datos:", data.decode("UTF-8"))
        return self.parse_maze_com(data)
    
    def parse_maze_com(self, xml_string):
        root = etree.fromstring(xml_string)
        message_type = root.attrib["messagetype"]
        id_attr = root.attrib["id"]
        mazecom = MazeCom(messagetype=message_type, id=id_attr)
        if message_type == "ACCEPT":
            #print("self.text:", self.text)
            accept_msg = AcceptMessageData()
            login_msg = root.find('AcceptMessage')
            accept = login_msg.find("accept").text
            errortypecode = login_msg.find("errortypeCode").text
            accept_msg.set_accept(accept)
            accept_msg.set_errortypeCode(errortypecode)
            mazecom.set_AcceptMessage(accept_msg)
        elif message_type == "LOGINREPLY":
            login = LoginReplyMessageData()
            login_id = root.find("LoginReplyMessage/newID").text
            login.set_newID(login_id)
            mazecom.set_LoginReplyMessage(login)
        elif message_type == "AWAITMOVE":
            # call parse_login function
            # add more elif blocks for other message types as needed
            board_to_fill = BoardData()
            board = root.find("AwaitMoveMessage/board")
            rows = board.findall("row")
            for row in rows:
                new_row = Row()
                cols = row.findall("col")
                for col in cols:
                    openings = col.find("openings")
                    top = openings.find("top").text 
                    bottom = openings.find("bottom").text
                    left = openings.find("left").text 
                    right = openings.find("right").text 
                    pin = col.find("pin")
                    if(pin.find("playerID") is not None):
                        playerID = int(pin.find("playerID").text)
                    else:
                        playerID = None
                    if(col.find("treasure") is not None):
                        treasure = col.find("treasure").text
                    else:
                        treasure = None
                    
                    # use the extracted data to create a col object and add it to the board
                    col_obj = Card(top=top, bottom=bottom, left=left, right=right, playerID=playerID, treasure=treasure)
                    new_row.add_col(col_obj)
                board_to_fill.add_row(new_row)
            
            # extract the shiftCard data
            shiftCard = root.find("AwaitMoveMessage/board/shiftCard")
            openings = shiftCard.find("openings")
            top = openings.find("top").text
            bottom = openings.find("bottom").text 
            left =  openings.find("left").text 
            right = openings.find("right").text 
            pin = shiftCard.find("pin")
            if(pin.find("playerID") is not None):
                playerID = int(pin.find("playerID").text)
            else:
                playerID = None
            if(shiftCard.find("treasure") is not None):
                treasure = shiftCard.find("treasure").text
            else:
                treasure = None
            shift_card_obj = Card(top=top, bottom=bottom, left=left, right=right, playerID=playerID, treasure=treasure)
            board_to_fill.set_shiftCard(shift_card_obj)

            treasures_to_go = root.find("AwaitMoveMessage/treasuresToGo")
            player = None
            treasures = None
            if(treasures_to_go is not None):
                if(treasures_to_go.find("player") is not None):
                    player = treasures_to_go.find("player").text
                else:
                    player = None
                if(treasures_to_go.find("treasures") is not None):
                    treasures = treasures_to_go.find("treasures").text
                else:
                    treasures = None
            treasures_to_go_data = TreasuresToGoData()
            treasures_to_go_data.set_player(player)
            treasures_to_go_data.set_treasures(treasures=treasures)


            
            forbidden = root.find("AwaitMoveMessage/board/forbidden")
            if forbidden != None:
                forbid_row = forbidden.find("row").text
                forbid_col = forbidden.find("col").text
                forbid_pos = Position(row=forbid_row, col=forbid_col)
                board_to_fill.set_forbidden(forbid_pos)
            else:
                board_to_fill.set_forbidden(None)
            
            treasure_to_find_next = root.find("AwaitMoveMessage/treasureToFindNext").text

            fin_board = Board(board_to_fill)
            mazecom.set_AwaitMoveMessage(AwaitMoveMessageData())
            mazecom.get_AwaitMoveMessage().set_board(fin_board)
            mazecom.get_AwaitMoveMessage().set_treasuresToGo(treasures_to_go_data)
            mazecom.get_AwaitMoveMessage().set_treasureToFindNext(treasure_to_find_next)

            #board_to_fill.set
        elif message_type == "DISCONNECT":
            disc_name = root.find("DisconnectMessage/name").text
            error_type_code = root.find("DisconnectMessage/errortypeCode").text
            mazecom.set_DisconnectMessage(DisconnectMessageData())
            mazecom.get_DisconnectMessage().set_name(disc_name)
            mazecom.get_DisconnectMessage().set_errortypeCode(error_type_code)
        elif message_type == "MOVEINFO":
            mazecom.set_MoveInfoMessage(MoveInfoData())
        elif message_type == "WIN":
            mazecom.set_WinMessage(WinMessage=WinMessageData())
        elif message_type == "GAMESTATUS":
            mazecom.set_GameStatusMessage(GameStatusMessage=GameStatusData())
        else:
            raise ValueError(f"Invalid message type: {message_type}")

        return mazecom