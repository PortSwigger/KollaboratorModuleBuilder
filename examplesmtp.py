import base64 
import re 
print("__interactionType__")
if("__interactionType__" == "SMTP"):
    a = "__smtpConversationB64__" 
    conversation = base64.b64decode(a) 
    lines = str(conversation,"utf-8").split("\r\n") 
    reg = re.compile("otp is (.*)") 
    for line in lines: 
        #print(str(reg.match(line)) + line) 
        if reg.match(line): 
            otp = reg.search(line) 
            print( "otp = " + str(otp.group(1)))
            #############################################################
            ##   write code to use the otp or extracted message data  ##
            #############################################################
