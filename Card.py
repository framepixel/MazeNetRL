from enum import Enum
from mazeCom import CardData, Openings, Pin
import ast

class Card(CardData):
    def __init__(self, c:CardData=None, shape=None, orientation=None, treasure=None, top=None, bottom=None,
                 left=None, right=None, playerID=None):
        super().__init__()
        if c != None:
            self.set_openings(Openings())
            try:
                self.get_openings().set_bottom(ast.literal_eval(c.get_openings().get_bottom()))
                self.get_openings().set_left(ast.literal_eval(c.get_openings().get_left()))
                self.get_openings().set_right(ast.literal_eval(c.get_openings().get_right()))
                self.get_openings().set_top(ast.literal_eval(c.get_openings().get_top()))
            except:
                self.get_openings().set_bottom(c.get_openings().get_bottom())
                self.get_openings().set_left(c.get_openings().get_left())
                self.get_openings().set_right(c.get_openings().get_right())
                self.get_openings().set_top(c.get_openings().get_top())
            self.set_treasure(c.get_treasure())
            self.set_pin(Pin())
            if (c.get_pin() != None):
                self.pin.get_playerID().extend(c.get_pin().get_playerID())
            else:
                self.set_pin(None)
            
        elif shape != None and orientation != None:
            self.set_openings(Openings())
            self.set_pin(Pin())
            if(shape.name == 'I'):
                if(orientation == "D0" or orientation == "D180"):
                    self.openings.set_bottom(True)
                    self.openings.set_top(True)
                    self.openings.set_left(False)
                    self.openings.set_right(False)

                elif(orientation == "D90" or orientation == "D270"):
                    self.openings.set_bottom(False)
                    self.openings.set_top(False)
                    self.openings.set_left(True)
                    self.openings.set_right(True)
                else:
                    raise Exception("Ungültige Drehung. Erlaub sind nur (D0,D90,D180,D270)")
            
            elif(shape.name == 'L'):
                if(orientation == "D0"):
                    self.openings.set_bottom(False)
                    self.openings.set_top(True)
                    self.openings.set_left(False)
                    self.openings.set_right(True)

                elif(orientation == "D90"):
                    self.openings.set_bottom(True)
                    self.openings.set_top(False)
                    self.openings.set_left(False)
                    self.openings.set_right(True)

                elif(orientation == "D180"):
                    self.openings.set_bottom(True)
                    self.openings.set_top(False)
                    self.openings.set_left(True)
                    self.openings.set_right(False)
                
                elif(orientation == "D270"):
                    self.openings.set_bottom(False)
                    self.openings.set_top(True)
                    self.openings.set_left(True)
                    self.openings.set_right(False)

                else:
                    raise Exception("Ungültige Drehung. Erlaub sind nur (D0,D90,D180,D270)")

            elif(shape.name == 'T'):
                if(orientation == "D0"):
                    self.openings.set_bottom(True)
                    self.openings.set_top(False)
                    self.openings.set_left(True)
                    self.openings.set_right(True)

                elif(orientation == "D90"):
                    self.openings.set_bottom(True)
                    self.openings.set_top(True)
                    self.openings.set_left(True)
                    self.openings.set_right(False)

                elif(orientation == "D180"):
                    self.openings.set_bottom(False)
                    self.openings.set_top(True)
                    self.openings.set_left(True)
                    self.openings.set_right(True)
                
                elif(orientation == "D270"):
                    self.openings.set_bottom(True)
                    self.openings.set_top(True)
                    self.openings.set_left(False)
                    self.openings.set_right(True)

                else:
                    raise Exception("Ungültige Drehung. Erlaub sind nur (D0,D90,D180,D270)")
            else:
                raise Exception("Ungültige Form. Erlaub sind nur (I,L,T)")
            self.set_treasure(treasure)
        else:
            self.set_openings(Openings())
            self.get_openings().set_bottom(ast.literal_eval(bottom.capitalize()))
            self.get_openings().set_left(ast.literal_eval(left.capitalize()))
            self.get_openings().set_right(ast.literal_eval(right.capitalize()))
            self.get_openings().set_top(ast.literal_eval(top.capitalize()))
            self.set_treasure(treasure)
            self.set_pin(Pin())
            if (playerID != None):
                self.pin.add_playerID(playerID)
            

            
    
    def getPossibleRotations(self):
        cards = []
        shape = self.getShape()
        treasure = self.get_treasure()
        if(shape == self.CardShape.L or shape == self.CardShape.T):
            cards.append(Card(shape=shape, orientation=Card.Orientation.D180.name, treasure=treasure))
            cards.append(Card(shape=shape, orientation=Card.Orientation.D270.name, treasure=treasure))
        elif(shape == self.CardShape.I):
            cards.append(Card(shape=shape, orientation=Card.Orientation.D0.name, treasure=treasure))
            cards.append(Card(shape=shape, orientation=Card.Orientation.D90.name, treasure=treasure))
        else:
            print("Card.invalidShape")
            
        return cards

    def getShape(self):
        open = [None] * 4
        open[0] = self.get_openings().get_top()
        open[1] = self.get_openings().get_right()
        open[2] = self.get_openings().get_bottom()
        open[3] = self.get_openings().get_left()
        
        indsum = 0
        numberOfOpenings = 0

        for i in range(len(open)):
            if (open[i]):
                indsum += i
                numberOfOpenings += 1
            
        
        if (numberOfOpenings == 2 and indsum % 2 == 0):
            return Card.CardShape.I
        elif (numberOfOpenings == 2 and indsum % 2 == 1):
            return Card.CardShape.L
        elif (numberOfOpenings == 3):
            return Card.CardShape.T
        else:
            raise Exception("Die Karte entspricht keiner gültigen Form")
        

    # def getOrientation(self):
    #     return self.card.getOrientation()

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