import grpc

import os, sys, inspect

current_dir = os.path.dirname(os.path.abspath(inspect.getfile(inspect.currentframe())))
parent_dir = os.path.dirname(current_dir)
sys.path.insert(0, parent_dir)
import PCSBasedComponentParameter_pb2
import PCSBasedComponentParameter_pb2_grpc

if __name__ == '__main__':
    # find parameterfile pcs in scenario
    f = open("scenario.txt", "r")
    lines = f.readlines();
    for line in lines:
        if line.startswith("paramfile"):
            componentName = line.replace("paramfile = ", "")[:-5]
        if line.startswith("gRPC_port"):
            gRPC_port = line.replace("gRPC_port = ", "").strip()

    params = []

    for i in range(6, len(sys.argv) - 1, 2):
        param = PCSBasedComponentParameter_pb2.PCSBasedParameterProto(key=sys.argv[i][1:].replace("-", ""),
                                                              value=sys.argv[i + 1])
        params.append(param)

    channel = grpc.insecure_channel("localhost:" + gRPC_port)
    stub = PCSBasedComponentParameter_pb2_grpc.PCSBasedOptimizerServiceStub(channel)

    cmp = PCSBasedComponentParameter_pb2.PCSBasedComponentProto(name=componentName, parameters=params)

    response = stub.Evaluate(cmp)
    channel.close()

    f = open("testout.txt", "a")
    f.write("### start run ###\n")
    for arg in sys.argv:
        f.write(arg + "\n")
    f.write("### end run ###\n\n")
    f.close()
    print('Result for SMAC: SUCCESS, -1, -1, %f, %s' % (response.result, "5"))
