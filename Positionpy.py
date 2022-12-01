from de.fhac.mazenet.server.game import Position as Ps

from PositionDatapy import PositionData

class Position (PositionData):
    def __init__(self, position=None, row=None, col=None):
        if position != None:
            self.position = Ps(position)
        elif row != None and col != None:
            self.position = Ps(row, col)
        else:
            self.position = Ps()
        
    def getPossiblePositionsForShiftcard(self):
        return self.position.getPossiblePositionsForShiftcard()

    def isLooseShiftPosition(self):
        return self.position.isLooseShiftPosition()

    def isOppositePosition(self, position):
        return self.position.isOppositePosition(position)

    def getOpposite(self):
        return self.position.getOpposite()

    def hashCode(self):
        return self.position.hashCode()

    def equals(self, obj):
        return self.position.equals(obj)

    def toString(self):
        return self.position.toString()