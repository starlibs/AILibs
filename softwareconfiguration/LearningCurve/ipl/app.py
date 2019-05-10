from flask import Flask, Response, jsonify, request
import pickle,os

app = Flask(__name__)
model = None

@app.route("/jaicore/web/api/v1/ipl/modelparams", methods=["Post"])
def extrapolate():
    try:
        x_values = request.get_json()["xValues"]
        y_values = request.get_json()["yValues"]
        anchorpoints = extract_anchorpoints(x_values, y_values)
        if not anchorpoints:
            return Response("Invalid anchorpoints", status=500)
        else:
            prediction = model.predict(anchorpoints)
            a = prediction[0][0]
            b = prediction[0][1]
            c = prediction[0][2]
            return jsonify({"a": a, "b": b, "c": c})
    except Exception as inst:
        return Response("Error: " + str(inst), status=500)

def extract_anchorpoints(x_values, y_values):
    y_8, y_16, y_64, y_128 = -1, -1, -1, -1
    for x, y in zip(x_values, y_values):
        if x == 8:
            y_8 = y
        elif x == 16:
            y_16 = y
        elif x == 64:
            y_64 = y
        elif x == 128:
            y_128 = y
    if y_8 != -1 and y_16 != -1 and y_64 != -1 and y_128 != -1:
        return [[y_8, y_16, y_64, y_128]]
    else:
        return False

if __name__ == "__main__":
    model = pickle.load( open( "InversePowerLawModel.pcl", "rb" ) )
    if 'IPL_PORT' in os.environ:
        port = os.environ['IPL_PORT']
    else:
        port = 8081
    app.run("0.0.0.0", port=port, debug=False)
