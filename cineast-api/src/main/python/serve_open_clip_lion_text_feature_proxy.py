from waitress import serve
import open_clip_lion_text_feature_proxy as app
import argparse


def main(args):
    print(f"Serving on {args.ip}, listen on {args.port}. Clip on {args.device}")
    serve(app.app, host=args.ip, port=args.port)


if __name__ == '__main__':
    print("start server")
    parser = argparse.ArgumentParser(formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument('--device', type=str, help='Device to use for feature extraction.', default='cpu')
    parser.add_argument('--port', type=int, help='Port to listen on.', default=8888)
    parser.add_argument('--ip', type=str, help='Ip to serve on.', default='localhost')
    args = parser.parse_args()

    main(args)
