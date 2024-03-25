import base64
import os.path

import torch
import open_clip
from flask import Flask, request
import json
from PIL import Image
import base64
from io import BytesIO
from datetime import datetime
import requests
import argparse


# parser = argparse.ArgumentParser(formatter_class=argparse.ArgumentDefaultsHelpFormatter)
# parser.add_argument('--device', type=str, help='Device to use for feature extraction.', default='cpu')
# parser.add_argument('--port', type=int, help='Port to listen on.', default=8888)
#
# args = parser.parse_args()
model, preprocess_train , preprocess_val = open_clip.create_model_and_transforms('xlm-roberta-base-ViT-B-32', pretrained='laion5b_s13b_b90k')
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
model = model.to(device)
tokenizer = open_clip.get_tokenizer('xlm-roberta-base-ViT-B-32')

app = Flask(__name__)
app.secret_key = 'BAD_SECRET_KEY'


@app.route('/heartbeat', methods=['GET'])
def hearthbeat():
    return "Beat dev"


@app.route('/', methods=['POST', 'GET'])
def handle_request():
    if request.method == 'POST':
        query = request.form['query']
        if query is None:
            return "[]"

        return json.dumps(feature(query).tolist())

    return "requests only supported vis POST"


@app.route('/image', methods=['POST'])
def handle_image_request():
    image_data = request.form['image']
    debug_storeimage = False

    dateTimeObj = datetime.now()
    file_name_for_base64_data = dateTimeObj.strftime("%d-%b-%Y--(%H-%M-%S)")

    print("try to embed image :" + file_name_for_base64_data)

    # File naming process for directory form <file_name.jpg> data.
    # We are taken the last 8 characters from the url string.
    # file_name_for_regular_data = url[-10:-4]
    featureVector = None
    try:
        # Base64 DATA
        if "data:image/jpeg;base64," in image_data:
            base_string = image_data.replace("data:image/jpeg;base64,", "")
            decoded_img = base64.b64decode(base_string)
            img = Image.open(BytesIO(decoded_img))
            featureVector = imagefeature(img)
            if debug_storeimage == True:
                file_name = file_name_for_base64_data + ".jpg"
                img.save(f"./debugImages/{file_name}", "jpg")


        # Base64 DATA
        elif "data:image/png;base64," in image_data:
            base_string = image_data.replace("data:image/png;base64,", "")
            decoded_img = base64.b64decode(base_string)
            img = Image.open(BytesIO(decoded_img))
            featureVector = imagefeature(img)
            if debug_storeimage == True:
                file_name = file_name_for_base64_data + ".png"
                img.save(f"./debugImages/{file_name}", "png")


        # Regular URL Form DATA
        else:
            response = requests.get(image_data)
            img = Image.open(BytesIO(response.content)).convert("RGB")
            featureVector = imagefeature(img)
            featureVector = imagefeature(img)
            if debug_storeimage == True:
                file_name = "file_name_for_regular_data" + ".jpg"
                img.save(f"./debugImages/{file_name}", "jpeg")

        status = "Image has been succesfully sent to the server."
    except Exception as ex:
        status = "Error! = " + str(ex)
        print("Error! = " + str(ex))
        return status


    return json.dumps(featureVector.tolist())


def imagefeature(imagequery):
    device = "cuda" if torch.cuda.is_available() else "cpu"
    image = preprocess_val(imagequery).unsqueeze(0).to(device)
    with torch.no_grad():
        image_features = model.encode_image(image)
        image_features /= image_features.norm(dim=-1, keepdim=True)
        return image_features.cpu().numpy().flatten()


def feature(query):
    device = "cuda" if torch.cuda.is_available() else "cpu"
    text = tokenizer(query).to(device)
    with torch.no_grad():
        text_features = model.encode_text(text)
        text_features /= text_features.norm(dim=-1, keepdim=True)
        return text_features.cpu().numpy().flatten()


if __name__ == '__main__':
    app.run(port=8888)
