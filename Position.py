from mazeCom import PositionData
import numpy as np

class Position (PositionData):
    def __init__(self, position: PositionData=None, row=None, col=None):
        super().__init__()
        if position != None:
            self.row = int(position.get_row())
            self.col = int(position.get_col())
        elif row != None and col != None:
            self.row = int(row)
            self.col = int(col)
        else:
            self.row = -1
            self.col = -1

    def equals(self, obj):
        if (self == obj):
            return True
        if (obj == None):
            return False
        other = Position(obj)
        if (self.col != other.col):
            return False
        if (self.row != other.row):
            return False
        return True

    def toString(self):
        return "(" + self.col + "," + self.row + ")"

    def getOpposite(self):
        if (self.row % 6 == 0):
            return Position(row=(self.row + 6) % 12, col=self.col)
        elif (self.col % 6 == 0):
            return Position(row=self.row, col=(self.col + 6) % 12)
        else:
            return None

    @staticmethod
    def getPossiblePositionsForShiftcard():
        positions = []
        for a in [0, 6] :
            for b in [1, 3, 5] :
                positions.append(Position(row=a, col=b))
                positions.append(Position(row=b, col=a))

        return positions
    
    def to_array(self):
        return np.array([self.col, self.row])