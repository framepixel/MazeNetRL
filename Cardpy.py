from de.fhac.mazenet.server.game import Card as Cd
from enum import Enum
from CardDatapy import CardData

class Card(CardData):
    def __init__(self, c=None, shape=None, orientation=None, treasure=None):
        super().__init__()
        if c != None:
            self.card = Cd(c)
        else:
            self.card = Cd(shape, orientation, treasure)
    
    def getPossibleRotations(self):
        return self.card.getPossibleRotations()

    def equals(self, o):
        return self.card.equals(o)

    def hashCode(self):
        return self.card.hashCode()

    def toString(self):
        return self.card.toString()

    def getShape(self):
        return self.card.getShape()

    def getOrientation(self):
        return self.card.getOrientation()

    class CardShape(Enum):
        L = 0
        T = 1
        I = 2
    
    class Orientation(Enum) :
        D0 = 0
        D90 = 1
        D180 = 2
        D270 = 3
        
        def __init__(self, value) :
            self.value = value

        @staticmethod
        def fromValue(self, v) :
            for c in self.values() :
                if (c.value == v) :
                    return c
            raise Exception("".join(v))

        def value(self) :
            return self.value