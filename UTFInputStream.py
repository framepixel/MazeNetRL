import io

class UTFInputStream:
    def __init__(self, input_stream):
        self.input_stream = input_stream

    def read_utf8(self) -> str:
        the_bytes = self.read_n_bytes(4)
        size = int.from_bytes(the_bytes[:4], byteorder='big')
        bytes = self.input_stream.read(size)
        xml = bytes.decode("UTF-8")
        xml = xml.split("?>",1)[-1]  # remove xml declaration tag
        return xml

    def close(self):
        self.input_stream.close()

    def read_n_bytes(self, n: int) -> bytes:
        buffer = bytearray(n)
        read_count = 0
        while read_count < n:
            try:
                last_read_count = self.input_stream.readinto(buffer[read_count:])
            except:
                import traceback
                print(traceback.format_exc())
            if last_read_count == 0:
                raise EOFError("EOF reached while reading bytes")
            read_count += last_read_count
        return bytes(buffer)

