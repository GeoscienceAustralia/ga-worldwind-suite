package org.csiro.examples.model;

import gov.nasa.worldwind.geom.Position;

/**
 * @author ringo-wathelet 2008-06-11
 */
public interface Adjustable {
    void setYaw(double val);
    double getYaw();
    void setRoll(double val);
    double getRoll();
    void setPitch(double val);
    double getPitch();
    void setSize(double val);
    double getSize();
    void setPosition(Position val);
    Position getPosition();
}
