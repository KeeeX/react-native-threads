import {
  NativeModules,
  DeviceEventEmitter,
} from "react-native";

const {ThreadSelfManager} = NativeModules;

const self = {
  onmessage: null,

  postMessage: message => {
    if (!message) {
      return Promise.resolve();
    }
    return ThreadSelfManager.postMessage(message);
  },
};

DeviceEventEmitter.addListener("ThreadMessage", message => {
  Boolean(message) && self.onmessage && self.onmessage(message);
});

export default self;
