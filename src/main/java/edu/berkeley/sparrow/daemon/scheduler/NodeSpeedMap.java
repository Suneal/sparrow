package edu.berkeley.sparrow.daemon.scheduler;

public class NodeSpeedMap {
    public String node;
    public double workerSpeed;

    public NodeSpeedMap() {

    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public double getWorkerSpeed() {
        return workerSpeed;
    }

    public void setWorkerSpeed(double workerSpeed) {
        this.workerSpeed = workerSpeed;
    }

    public NodeSpeedMap(String node, double workerSpeed) {
        this.node = node;
        this.workerSpeed = workerSpeed;
    }
}
