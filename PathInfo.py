class PathInfo:
    def __init__(self, stepsFromSource=None, cameFrom=None):
        if stepsFromSource != None and cameFrom != None:
            self.stepsFromSource = int(stepsFromSource)
            self.cameFrom = int(cameFrom)
        else:
            self.stepsFromSource = -1
            self.cameFrom = None
    
    def setStepsFromSource(self, stepsFromSource):
        self.stepsFromSource = stepsFromSource

    def setCameFrom(self, cameFrom):
        self.cameFrom = cameFrom

    def getStepsFromSource(self):
        return self.stepsFromSource

    def getCameFrom(self):
        return self.cameFrom
    