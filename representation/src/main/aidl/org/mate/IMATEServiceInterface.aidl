// IMATEServiceInterface.aidl
package org.mate;

import org.mate.IRepresentationLayerInterface;

interface IMATEServiceInterface {
    void registerRepresentationLayer(IRepresentationLayerInterface representationLayer, in IBinder deathListener);
    void reportAvailableActions(in List<String> actions);
}