from mazeCom import BoardData, Row, Pin, PositionData, CardData, MoveMessageData
from Position import Position
from Card import Card
from PathInfo import PathInfo
import traceback
import copy

class Board (BoardData):

    currentTreasure = None

    def __init__(self, boardData: BoardData=None):
        super().__init__()
        forbiddenPositionData = boardData.forbidden
        self.forbidden = Position(position=forbiddenPositionData) if forbiddenPositionData else None
        self.foundTreasures.extend(boardData.foundTreasures)
        self.shiftCard = Card(c=boardData.shiftCard)
        for i in range(7):
            self.row.insert(i, Row())
            for j in range(7):
                self.row[i].col.insert(j, boardData.row[i].col[j])

    def set_card(self, row, col, card):
        self.row[row].col.pop(col)
        self.row[row].col.insert(col, card)

    def get_card(self, row, col) -> CardData:
        return self.row[row].col[col]

    def get_num_players(self):
        num = 0
        for r in self.row:
            for col in r.col:
                if col.get_pin() is not None:
                    num += len(col.get_pin().get_playerID())
        return num


    def proceed_shift(self, move: MoveMessageData):
        shift_position = Position(position=move.shiftPosition)
        
        if shift_position.col % 6 == 0:
            if shift_position.row % 2 == 1:
                # horizontal shift
                row = shift_position.row
                start = (shift_position.col + 6) % 12
                self.set_shiftCard(self.get_card(row, start))
                if start == 6:
                    for i in range(6, 0, -1):
                        card_to_set = self.get_card(row, i - 1)
                        self.set_card(row, i, card_to_set)
                else:  # start == 0
                    for i in range(0, 6):
                        self.set_card(row, i, self.get_card(row, i + 1))
        elif shift_position.row % 6 == 0:
            if shift_position.col % 2 == 1:
                # vertical shift
                col = shift_position.col
                start = (shift_position.row + 6) % 12
                self.set_shiftCard(self.get_card(start, col))
                if start == 6:
                    for i in range(6, 0, -1):
                        self.set_card(i, col, self.get_card(i - 1, col))
                else:  # start == 0
                    for i in range(0, 6):
                        self.set_card(i, col, self.get_card(i + 1, col))
        self.forbidden = shift_position.getOpposite()
        shift_card = Card(c=move.shiftCard)
        if len(self.shiftCard.pin.playerID) != 0:
            temp = self.shiftCard.pin
            self.shiftCard.pin = Pin()
            shift_card.pin = temp
        else:
            #print("self.shiftCard.pin.playerID:", self.shiftCard.pin.playerID)
            pass
        self.set_card(shift_position.row, shift_position.col, shift_card)

    def fakeShift(self, move):
        fake = copy.deepcopy(self)
        #print("board before shift:", fake)
        fake.proceed_shift(move)
        #print("board after shift:", fake)
        return fake

    def clone(self):
        clone = Board(self)
        clone.currentTreasure = self.currentTreasure
        return clone

    def getAllReachablePositions(self, position) -> list:
        reachable_positions = []
        reachable = self.getAllReachablePositionsMatrix(position=position)
        for i in range(len(reachable)):
            for j in range(len(reachable[0])):
                if reachable[i][j].getStepsFromSource() > -1:
                    reachable_positions.append(Position(row=i, col=j))
        return reachable_positions

    def getAllReachablePositionsMatrix(self, position, reachable=None, step=None, came_from=None):
        if position is not None and reachable is not None and step is not None:
            reachable[position.row][position.col].setStepsFromSource(step)
            reachable[position.row][position.col].setCameFrom(came_from)
            direct_reachable_positions = self.get_direct_reachable_positions(position=position)
            for p1 in direct_reachable_positions:
                if reachable[p1.row][p1.col].getStepsFromSource() < 0 or reachable[p1.row][p1.col].getStepsFromSource() > step + 1:
                    self.getAllReachablePositionsMatrix(p1, reachable, step + 1, position)
            return reachable
        else:
            reachable = [[PathInfo() for j in range(7)] for i in range(7)]
            return self.getAllReachablePositionsMatrix(Position(position=position), reachable, 0, None)

    def get_direct_reachable_positions(self, position:Position):
        positions = []
        k = self.get_card(position.get_row(), position.get_col())
        openings = k.get_openings()
        if openings.get_left():
            if position.get_col() - 1 >= 0 and self.get_card(position.get_row(), position.get_col() - 1).get_openings().get_right():
                positions.append(Position(row=position.get_row(), col=position.get_col() - 1))
        if openings.get_top():
            if position.get_row() - 1 >= 0 and self.get_card(position.get_row() - 1, position.get_col()).get_openings().get_bottom():
                positions.append(Position(row=position.get_row() - 1, col=position.get_col()))
        if openings.get_right():
            if position.get_col() + 1 <= 6 and self.get_card(position.get_row(), position.get_col() + 1).get_openings().get_left():
                positions.append(Position(row=position.get_row(), col=position.get_col() + 1))
        if openings.get_bottom():
            if position.get_row() + 1 <= 6 and self.get_card(position.get_row() + 1, position.get_col()).get_openings().get_top():
                positions.append(Position(row=position.get_row() + 1, col=position.get_col()))
        return positions

    def findPlayer(self, player_id):
        for i in range(7):
            for j in range(7):
                pins_on_card = self.get_card(i, j).get_pin()
                for pin in pins_on_card.get_playerID():
                    if pin == player_id:
                        position = Position(row=i, col=j)
                        return position
        # Pin not found.
        # Should not happen
        return None

    def findTreasure(self, treasureData):
        if treasureData is None:
            raise Exception("Board.treasureIsNull")

        for i in range(7):
            for j in range(7):
                treasure = self.get_card(i, j).get_treasure()
                if treasure == treasureData:
                    position = PositionData()
                    position.row = i
                    position.col = j
                    return position

        # Treasure not found, which can only mean that the treasure is on a
        # sliding card
        return None
    
    def get_treasure(self):
        return self.currentTreasure

    def set_treasure(self, t):
        self.currentTreasure = t


    def __str__(self):
        stringBuilder = "Board [currentTreasure={}]\n".format(self.currentTreasure)
        stringBuilder += " ------ ------ ------ ------ ------ ------ ------ \n"
        for i in range(len(self.get_row())):
            line1 = "|"
            line2 = "|"
            line3 = "|"
            line4 = "|"
            line5 = "|"
            line6 = "|"
            for j in range(len(self.get_row()[i].get_col())):
                card = self.get_card(i, j)
                if card.get_openings().get_top():
                    line1 += "##  ##|"
                    line2 += "##  ##|"
                else:
                    line1 += "######|"
                    line2 += "######|"
                if card.get_openings().get_left():
                    line3 += "  "
                    line4 += "  "
                else:
                    line3 += "##"
                    line4 += "##"
                if len(card.get_pin().get_playerID()) != 0:
                    line3 += "S"
                else:
                    line3 += " "
                if card.get_treasure() is not None:
                    name = card.get_treasure()
                    if name[1] == 'Y':
                        line3 += "T"
                    elif name[1] == 'T':
                        line3 += "S"
                    line4 += name[-2:]
                else:
                    line3 += " "
                    line4 += "  "
                if card.get_openings().get_right():
                    line3 += "  |"
                    line4 += "  |"
                else:
                    line3 += "##|"
                    line4 += "##|"
                if card.get_openings().get_bottom():
                    line5 += "##  ##|"
                    line6 += "##  ##|"
                else:
                    line5 += "######|"
                    line6 += "######|"
            stringBuilder += line1 +"\n"
            stringBuilder += line2 +"\n"
            stringBuilder += line3 +"\n"
            stringBuilder += line4 +"\n"
            stringBuilder += line5 +"\n"
            stringBuilder += line6 +"\n"
            stringBuilder += " ------ ------ ------ ------ ------ ------ ------ \n"
        return str(stringBuilder)
