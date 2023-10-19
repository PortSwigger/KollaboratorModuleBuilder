import base64 
import re 
import requests
if("__interactionType__" == "HTTP"):
    a = "__httpRequestPathB64__"
    url = str(base64.b64decode(a) , "utf-8")
    reg = re.compile("(.*)token(.*)") 
    if(reg.match(url)):
        token = url.split("?")[1].split("=")[1]
        headers = { 'Token' : token }
        try:
            response = requests.get("https://example.com", headers=headers)
            if response.status_code == 200:
                print(response.text)
            else:
                print(f"Request failed with status code: {response.status_code}")
                print(response.text)
        except requests.exceptions.RequestException as e:
            print(f"Request failed with an exception: {e}")
