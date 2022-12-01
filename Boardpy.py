from de.fhac.mazenet.server.game import Board as Bo

from mazeCom import BoardData

class Board (BoardData):
    def __init__(self, boardData=None):
        if boardData == None:
            self.board = Bo()
        else:
            self.board = Bo(boardData)
    
    def generateInitialBoard(self):
        self.board.generateInitialBoard()
    
    def toString(self):
        return self.board.toString()

    def setCard(self, row, col, card):
        self.board.setCard(row, col, card)

    def getCard(self, row, col):
        return self.board.getCard(row, col)

    def proceedShift(self, move):
        self.board.proceedShift(move)

    def proceedTurn(self, move, currentPlayer):
        return self.board.proceedTurn(move, currentPlayer)

    def movePlayer(self, oldPosition, newPosition, playerID):
        return self.board.movePlayer(oldPosition, newPosition, playerID)
    
    def fakeShift(self, move):
        return self.board.fakeShift(move)

    def clone(self):
        return self.board.clone()
    
    def validateTransition(self, move, playerID):
        return self.board.validateTransition(move, playerID)

    def pathPossible(self, oldPositionData, newPositionData):
        return self.board.pathPossible(oldPositionData, newPositionData)

    def getAllReachablePositions(self, position):
        return self.board.getAllReachablePositions(position)

    def getAllReachablePositionsMatrix(self, position):
        return self.board.getAllReachablePositionsMatrix(position)

    def getAllReachablePositionsMatrix(self, position, rechable, step, cameFrom):
        return self.board.getAllReachablePositionsMatrix(position, rechable, step, cameFrom)

    def getDirectReachablePositions(self, position):
        return self.board.getDirectReachablePositions(position)

    def findPlayer(self, playerID):
        return self.findPlayer(playerID)

    def findTreasure(self, treasureData):
        return self.board.findTreasure(treasureData)

    def getTreasure(self):
        return self.board.getTreasure()

    def setTreasure(self, t):
        self.board.setTreasure(t)