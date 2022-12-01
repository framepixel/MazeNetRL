from de.fhac.mazenet.server.generated import CardData as Cdd

class CardData :

    def __init__(self):
        self.carddata = Cdd()
    
    def getOpenings(self):
        return self.carddata.getOpenings()
    def setOpenings(self, value):
        self.carddata.setOpenings(value)
    def getPin(self):
        return self.carddata.getPin()
    def setPin(self, value):
        self.carddata.setPin(value)
    def getTreasure(self):
        return self.carddata.getTreasure()
    def setTreasure(self, value):
        self.carddata.setTreasure(value)
    class Openings:
        def __init__(self):
            self.openings = Cdd.Openings()
        def  isTop(self):
            return self.openings.isTop()
        def setTop(self, value):
            self.openings.setTop(value)
        def  isBottom(self):
            return self.openings.isBottom()
        def setBottom(self, value):
            self.openings.setBottom(value)
        def  isLeft(self):
            return self.openings.isLeft()
        def setLeft(self, value):
            self.openings.setLeft(value)
        def  isRight(self):
            return self.openings.isRight()
        def setRight(self, value):
            self.openings.setRight(value)
    class Pin:
        #playerID = None
        def __init__(self):
            self.pin = Cdd.Pin()
            pass
        def  getPlayerID(self):
            # if (self.playerID == None):
            #     self.playerID =  []
            return self.pin.getPlayerID()