// IMATEServiceInterface.aidl
package org.mate.commons;

import org.mate.commons.IRepresentationLayerInterface;

interface IMATEServiceInterface {
    void registerRepresentationLayer(IRepresentationLayerInterface representationLayer, in IBinder deathListener);
}