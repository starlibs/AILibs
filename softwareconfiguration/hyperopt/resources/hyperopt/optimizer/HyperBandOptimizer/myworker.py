from hpbandster.core.worker import Worker
from ConfigSpace.read_and_write import pcs
import grpc

import os, sys, inspect

current_dir = os.path.dirname(os.path.abspath(inspect.getfile(inspect.currentframe())))
parent_dir = os.path.dirname(current_dir)
sys.path.insert(0, parent_dir)
import PCSBasedComponentParameter_pb2
import PCSBasedComponentParameter_pb2_grpc


class MyWorker(Worker):
    component = ''

    def __init__(self, *args, sleep_interval=0, component_name, gRPC_port, **kwargs):
        super().__init__(*args, **kwargs)

        self.sleep_interval = sleep_interval
        self.component_name = component_name
        self.gRPC_port = gRPC_port
        global component
        component = component_name

    def compute(self, config, budget, **kwargs):
        """
        Simple example for a compute function
        The loss is just a the config + some noise (that decreases with the budget)

        For dramatization, the function can sleep for a given interval to emphasizes
        the speed ups achievable with parallel workers.

        Args:
            config: dictionary containing the sampled configurations by the optimizer
            budget: (float) amount of time/epochs/etc. the model can use to train

        Returns:
            dictionary with mandatory fields:
                'loss' (scalar)
                'info' (dict)
        """

        params = []

        for k, v in config.items():
            param = PCSBasedComponentParameter_pb2.PCSBasedParameterProto(key=k, value=str(v))
            params.append(param)

        channel = grpc.insecure_channel("localhost:" + str(self.gRPC_port))
        stub = PCSBasedComponentParameter_pb2_grpc.PCSBasedOptimizerServiceStub(channel)

        cmp = PCSBasedComponentParameter_pb2.PCSBasedComponentProto(name=self.component_name, parameters=params)

        response = stub.Evaluate(cmp)
        channel.close()

        # res = numpy.clip(config['x'] + numpy.random.randn() / budget, config['x'] / 2, 1.5 * config['x'])
        # config['weka.classifiers.bayes.BayesNet.Q']
        # time.sleep(self.sleep_interval)
        print(response.result)

        return ({
            'loss': float(response.result),  # this is the a mandatory field to run hyperband
            'info': response.result  # can be used for any user-defined information - also mandatory
        })

    @staticmethod
    def get_configspace():
        with open(component + '.pcs', 'r') as fh:
            config_space = pcs.read(fh)
            return config_space
