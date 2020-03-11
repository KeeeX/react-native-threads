import {
  NativeModules,
  DeviceEventEmitter,
} from "react-native";

const {ThreadManager} = NativeModules;

export default class Thread {
  constructor(jsPath) {
    if (!jsPath || !jsPath.endsWith(".js")) {
      throw new Error("Invalid path for thread. Only js files are supported");
    }
    this.jsPath = jsPath;
    this.id = null;
  }

  start() {
    return ThreadManager.startThread(this.jsPath.replace(".js", ""))
      .then(id => {
        DeviceEventEmitter.addListener(`Thread${id}`, message => {
          Boolean(message) && this.onmessage && this.onmessage(message);
        });
        this.id = id;
      });
  }

  checkId() {
    if (this.id === null) {
      throw new Error("NULL id");
    }
  }

  postMessage(message) {
    this.checkId();
    return ThreadManager.postThreadMessage(this.id, message);
  }

  terminate() {
    this.checkId();
    ThreadManager.stopThread(this.id);
  }
}
