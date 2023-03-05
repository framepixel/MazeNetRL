# from PyByteBuffer import ByteBuffer

# class UTFOutputStream :
#     outputStream = None
#     def __init__(self, outputStream) :
#         self.outputStream = outputStream
#     def writeUTF8(self, text) :  #Throws IOException
#         bytes = bytes(text, 'utf-8')
#         byteBuffer = ByteBuffer.allocate(4)
#         # Transform to network order
#         #byteBuffer.order(BIG_ENDIAN)
#         byteBuffer.put(len(bytes), endianness='big')
#         self.outputStream.write(byteBuffer.array())
#         self.outputStream.write(bytes)
#     def flush(self) :  #Throws IOException
#         self.outputStream.flush()
#     def close(self) :  #Throws IOException
#         self.outputStream.close()

import struct

class UTFOutputStream:
    def __init__(self, output_stream):
        self.output_stream = output_stream

    def write_utf8(self, text):
        #text = text.encode('UTF-8')
        #print("text:", text)
        self.output_stream.send(text)

    def flush(self):
        self.output_stream.flush()

    def close(self):
        self.output_stream.close()
