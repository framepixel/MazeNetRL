import xml.etree.ElementTree as ET
import xml.dom.minidom as minidom
from UTFOutputStream import UTFOutputStream
from mazeCom import MazeComMessagetype, MazeCom
import pickle
import struct

class XmlOutputStream(UTFOutputStream):
    def __init__(self, output_stream):
        super().__init__(output_stream)
        self.marshaller = ET.XML


    def write(self, maze_com):
        xml = self.maze_com_to_xml(maze_com)
        self.write_utf8(xml)

    def flatten_list(self, l):
        for el in l:
            if isinstance(el, list):
                yield from self.flatten_list(el)
            else:
                yield el

    def maze_com_to_xml(self, maze_com: MazeCom):#
        # Create an ET.Element object that represents the Java object
        root = ET.Element("MazeCom",
                            attrib={"messagetype": maze_com.get_messagetype().name,
                            "id": str(maze_com.get_id()),})
        # Add the child elements of the Java object to the ET.Element object
        if maze_com.get_LoginMessage() is not None:
            login_message_element = ET.SubElement(root, "LoginMessage")
            name_element = ET.SubElement(login_message_element, "name")
            name_element.text = maze_com.get_LoginMessage().get_name()
            #if maze_com.get_LoginMessage().get_role() == "MANAGER":
            role_element = ET.SubElement(login_message_element, "role")
            role_element.text = maze_com.get_LoginMessage().get_role()
        if maze_com.get_LoginReplyMessage() is not None:
            login_reply_message_element = ET.SubElement(root, "LoginReplyMessage")
            login_reply_message_element.text = maze_com.get_LoginReplyMessage()
        if maze_com.get_AwaitMoveMessage() is not None:
            pass
        if maze_com.get_MoveMessage() is not None:
            await_move_message_element = ET.SubElement(root, "MoveMessage")

            shift_pos_element = ET.SubElement(await_move_message_element, 'shiftPosition')
            row_shift_element = ET.SubElement(shift_pos_element, 'row')
            row_shift_element.text = str(maze_com.get_MoveMessage().get_shiftPosition().get_row())
            col_shift_element = ET.SubElement(shift_pos_element, 'col')
            col_shift_element.text = str(maze_com.get_MoveMessage().get_shiftPosition().get_col())

            new_pin_pos_element = ET.SubElement(await_move_message_element, 'newPinPos')
            row_shift_element = ET.SubElement(new_pin_pos_element, 'row')
            row_shift_element.text = str(maze_com.get_MoveMessage().get_newPinPos().get_row())
            col_shift_element = ET.SubElement(new_pin_pos_element, 'col')
            col_shift_element.text = str(maze_com.get_MoveMessage().get_newPinPos().get_col())

            shift_card_element = ET.SubElement(await_move_message_element, 'shiftCard')

            openings_element = ET.SubElement(shift_card_element, 'openings')
            top_element = ET.SubElement(openings_element, 'top')
            top_element.text = str(maze_com.get_MoveMessage().get_shiftCard().get_openings().get_top()).lower()
            bottom_element = ET.SubElement(openings_element, 'bottom')
            bottom_element.text = str(maze_com.get_MoveMessage().get_shiftCard().get_openings().get_bottom()).lower()
            left_element = ET.SubElement(openings_element, 'left')
            left_element.text = str(maze_com.get_MoveMessage().get_shiftCard().get_openings().get_left()).lower()
            right_element = ET.SubElement(openings_element, 'right')
            right_element.text = str(maze_com.get_MoveMessage().get_shiftCard().get_openings().get_right()).lower()

            pin_element = ET.SubElement(shift_card_element, 'pin')
            
            
            #if(maze_com.get_MoveMessage().get_shiftCard().get_pin().get_playerID() != []):
            for player in maze_com.get_MoveMessage().get_shiftCard().get_pin().get_playerID():
                player_id_element = ET.SubElement(pin_element, 'playerID')
                player_id_element.text = str(player)
            
            if maze_com.get_MoveMessage().get_shiftCard().get_treasure() != None:
                treasure_element = ET.SubElement(shift_card_element, 'treasure')
                treasure_element.text = maze_com.get_MoveMessage().get_shiftCard().get_treasure()
        if maze_com.get_MoveInfoMessage() is not None:
            move_info_message_element = ET.SubElement(root, "MoveInfoMessage")
            move_info_message_element.text = maze_com.get_MoveInfoMessage()
        if maze_com.get_GameStatusMessage() is not None:
            game_status_message_element = ET.SubElement(root, "GameStatusMessage")
            game_status_message_element.text = maze_com.get_GameStatusMessage()
        if maze_com.get_ControlServerMessage() is not None:
            control_server_message_element = ET.SubElement(root, "ControlServerMessage")
            player_count_element = ET.SubElement(control_server_message_element, "playerCount")
            player_count_element.text = str(maze_com.get_ControlServerMessage().get_playerCount())
            command_element = ET.SubElement(control_server_message_element, "command")
            command_element.text = maze_com.get_ControlServerMessage().get_command()
        if maze_com.get_AcceptMessage() is not None:
            accept_message_element = ET.SubElement(root, "AcceptMessage")
            accept_message_element.text = maze_com.get_AcceptMessage()
            accept_element = ET.SubElement(accept_message_element, "accept")
            accept_element.text = maze_com.get_AcceptMessage().get_accept()
            error_element = ET.SubElement(accept_message_element, "errortypeCode")
            error_element.text = maze_com.get_AcceptMessage().get_errortypeCode()
        if maze_com.get_WinMessage() is not None:
            win_message_element = ET.SubElement(root, "WinMessage")
            win_message_element.text = maze_com.get_WinMessage()
        if maze_com.get_DisconnectMessage() is not None:
            win_message_element = ET.SubElement(root, "DisconnectMessage")
            win_message_element.text = maze_com.get_DisconnectMessage()

        # if maze_com.get_ControlServerMessage() is not None:
        #     control_server_message_element = ET.SubElement(root, "ControlServerMessage")
        #     #control_server_message_element.text = maze_com.get_ControlServerMessage()
        #     player_count_element = ET.SubElement(control_server_message_element, "playerCount")
        #     player_count_element.text = maze_com.get_ControlServerMessage().get_playerCount()
        #     command_element = ET.SubElement(control_server_message_element, "command")
        #     command_element.text = maze_com.get_ControlServerMessage().get_command()
            


        # Add more child elements as needed...
        xml = ET.tostring(root, encoding="UTF-8")
        xml_str = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>' + xml.decode('utf-8')
        bytes_data = struct.pack('>i', len(xml_str)) + xml_str.encode('utf-8')
        return bytes_data


#import UTFOutputStream
# from jpype.pickle import JPickler, JUnpickler

# #jpype.startJVM(jpype.getDefaultJVMPath())

# #Jpickle = jpype.JClass("com.github.jpype.jpickle.Jpickle")

# class XmlOutputStream(UTFOutputStream):
#     def __init__(self, output_stream):
#         super().__init__(output_stream)
    
#     def maze_com_to_xml(self, maze_com):
#         # Serialize the MazeCom object to a byte stream using Jpickle
#         maze_com_bytes = JPickler.dump(maze_com)
        
#         # Return the byte stream as a UTF-8 encoded string
#         return maze_com_bytes.decode("utf-8")
    
#     def write(self, maze_com):
#         # Serialize the MazeCom object to a byte stream using Jpickle
#         maze_com_bytes = JPickler.dump(maze_com)
        
#         # Write the length of the byte stream to the output stream
#         self.write_utf8(str(len(maze_com_bytes)))
        
#         # Write the byte stream to the output stream
#         self.output_stream.write(maze_com_bytes)
#         self.output_stream.flush()


# from UTFOutputStream import UTFOutputStream
# import jpype

# class XmlOutputStream(UTFOutputStream):
#     def __init__(self, output_stream):
#         super().__init__(output_stream)
#         jpype.addClassPath("./maze-server/src/main/java/de/fhac/mazenet/server/generated") # Replace this with the actual path to your generated classes folder
#         jpype.addClassPath("./jaxb-api-2.3.1.jar") # Replace this with the actual path to the JAXB API jar file
#         #self.marshaller = jpype.JClass("javax.xml.bind.JAXBContext").newInstance("de.fhac.mazenet.server.generated") # Replace "de.fhac.mazenet.server.generated" with the actual package name of your generated classes
#         self.jaxb_context = jpype.JClass("javax.xml.bind.JAXBContext").newInstance("de.fhac.mazenet.server.generated") # Replace "de.fhac.mazenet.server.generated" with the actual package name of your generated classes
    
#     def write(self, maze_com):
#         xml = self.maze_com_to_xml(maze_com)
#         self.write_utf8(xml)
#         print(f"XmlOutputStream.SendMessageTypToID{str(maze_com.get_message_type().value())} {str(maze_com.getId())}")
#         self.flush()

#     def maze_com_to_xml(self, maze_com):
#         # Create a Marshaller object
#         marshaller = self.jaxb_context.createMarshaller()

#         # Marshal the Java object to XML
#         with open("output.xml", "w") as out:
#             marshaller.marshal(maze_com, out)

# from UTFOutputStream import UTFOutputStream

# class XmlOutputStream(UTFOutputStream):
#     def __init__(self, output_stream: io.RawIOBase):
#         super().__init__(output_stream)
#         try:
#             self.marshaller = tostring
#             self.marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, True)
#         except ParseError as e:
#             print(e)
#             print(Messages.getString("XmlOutputStream.ErrorInitialisingJAXBComponent"))

#     def write(self, mazeCom: Any):
#         try:
#             self.write_utf8(self.maze_com_to_xml(mazeCom))
#             print(String.format(Messages.getString("XmlOutputStream.SendMessageTypToID"),
#                     mazeCom.get_messagetype(), mazeCom.get_id()))
#             self.flush()
#         except Exception as e:
#             print(Messages.getString("XmlOutputStream.errorSendingMessage"))
#             print(e)

#     def maze_com_to_xml(self, mazeCom: Any) -> str:
#         return self.marshaller(mazeCom)

# class XmlOutputStream(UTFOutputStream):
#     def __init__(self, output_stream):
#         super().__init__(output_stream)

#     def write(self, maze_com):
#         xml = self.maze_com_to_xml(maze_com)
#         self.write_utf8(xml)
#         self.flush()

#     def maze_com_to_xml(self, maze_com) -> str:
#         root = ET.Element("MazeCom", attrib={"messagetype": maze_com.get_messagetype().name, "id": str(maze_com.get_id())})
        
#         if maze_com.get_LoginMessage() is not None:
#             login_message_element = ET.SubElement(root, "LoginMessage")
#             login_message_element.text = maze_com.get_LoginMessage()
#         if maze_com.get_LoginReplyMessage() is not None:
#             login_reply_message_element = ET.SubElement(root, "LoginReplyMessage")
#             login_reply_message_element.text = maze_com.get_LoginReplyMessage()
#         if maze_com.get_AwaitMoveMessage() is not None:
#             await_move_message_element = ET.SubElement(root, "AwaitMoveMessage")
#             await_move_message_element.text = maze_com.get_AwaitMoveMessage()
#         if maze_com.get_MoveMessage() is not None:
#             move_message_element = ET.SubElement(root, "MoveMessage")
#             move_message_element.text = maze_com.get_MoveMessage()
#         if maze_com.get_MoveInfoMessage() is not None:
#             move_info_message_element = ET.SubElement(root, "MoveInfoMessage")
#             move_info_message_element.text = maze_com.get_MoveInfoMessage()
#         if maze_com.get_GameStatusMessage() is not None:
#             game_status_message_element = ET.SubElement(root, "GameStatusMessage")
#             game_status_message_element.text = maze_com.get_GameStatusMessage()
#         if maze_com.get_ControlServerMessage() is not None:
#             control_server_message_element = ET.SubElement(root, "ControlServerMessage")
#             control_server_message_element.text = maze_com.get_ControlServerMessage()
#         if maze_com.get_AcceptMessage() is not None:
#             accept_message_element = ET.SubElement(root, "AcceptMessage")
#             accept_message_element.text = maze_com.get_AcceptMessage()
#         if maze_com.get_WinMessage() is not None:
#             win_message_element = ET.SubElement(root, "WinMessage")
#             win_message_element.text = maze_com.get_WinMessage()
#         if maze_com.get_DisconnectMessage() is not None:
#             win_message_element = ET.SubElement(root, "DisconnectMessage")
#             win_message_element.text = maze_com.get_DisconnectMessage()

       
#         xml = ET.tostring(root)
#         return minidom.parseString(xml).toprettyxml()