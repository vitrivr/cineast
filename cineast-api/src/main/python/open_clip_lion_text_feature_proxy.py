import torch
from PIL import Image
import open_clip
from flask import Flask, request
import json
import argparse

parser = argparse.ArgumentParser(formatter_class=argparse.ArgumentDefaultsHelpFormatter)
parser.add_argument('--device', type=str, help='Device to use for feature extraction.', default='cpu')
parser.add_argument('--port', type=int, help='Port to listen on.', default=8888)

args = parser.parse_args()

model, _, _ = open_clip.create_model_and_transforms('xlm-roberta-base-ViT-B-32', pretrained='laion5b_s13b_b90k')
model = model.to(args.device)
tokenizer = open_clip.get_tokenizer('xlm-roberta-base-ViT-B-32')

app = Flask(__name__)

@app.route('/',methods = ['POST', 'GET'])
def handle_request():
  if request.method == 'POST':
    query = request.form['query']
    if query is None:
      return "[]"

    return json.dumps(feature(query).tolist())

  return "requests only supported vis POST"


def feature(query):
  text = tokenizer(query).to(args.device)
  with torch.no_grad():
    text_features = model.encode_text(text)
    text_features /= text_features.norm(dim=-1, keepdim=True)
    return text_features.cpu().numpy().flatten()

if __name__ == '__main__':
  app.run(port = args.port)
