package com.metsci.glimpse.examples.layers;

import static com.metsci.glimpse.util.PredicateUtils.notNull;
import static com.metsci.glimpse.util.PredicateUtils.require;

import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxisListener1D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.layers.GeoLayer;
import com.metsci.glimpse.layers.Layer;
import com.metsci.glimpse.layers.LayeredGeo;
import com.metsci.glimpse.layers.LayeredScenario;
import com.metsci.glimpse.layers.LayeredTimeline;
import com.metsci.glimpse.layers.TimelineLayer;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;

public class ExampleLayer implements Layer, GeoLayer, TimelineLayer
{

    protected GeoProjection geoProj;
    protected Epoch timelineEpoch;

    protected ExampleGeoPainter geoPainter;

    protected TimePlotInfo timelineRow;
    protected TaggedAxisListener1D timeAxisListener;
    protected ExampleTimelinePainter timelinePainter;


    public ExampleLayer( )
    {
        this.geoProj = null;
        this.timelineEpoch = null;

        this.geoPainter = null;

        this.timelineRow = null;
        this.timeAxisListener = null;
        this.timelinePainter = null;
    }

    // WIP: Take lat/lon instead of x,y
    public void addPoint( long time_PMILLIS, float x_SU, float y_SU, float z_SU )
    {
        // WIP: This only works if the painters already exist, which isn't true until the layer has been installed
        this.geoPainter.addPoint( time_PMILLIS, x_SU, y_SU, z_SU );
        this.timelinePainter.addPoint( time_PMILLIS, x_SU, y_SU, z_SU );
    }

    @Override
    public void init( LayeredScenario scenario )
    {
        this.geoProj = require( scenario.geoProj, notNull );
        this.timelineEpoch = require( scenario.timelineEpoch, notNull );
    }

    @Override
    public void installToGeo( LayeredGeo geo )
    {
        this.geoPainter = new ExampleGeoPainter( this.geoProj, this.timelineEpoch );
        geo.dataPainter.addPainter( this.geoPainter );
    }

    @Override
    public void uninstallFromGeo( LayeredGeo geo, GlimpseContext context )
    {
        geo.dataPainter.removePainter( this.geoPainter );
        this.geoPainter.dispose( context );
        this.geoPainter = null;
    }

    @Override
    public void installToTimeline( LayeredTimeline timeline )
    {
        this.timelinePainter = new ExampleTimelinePainter( this.timelineEpoch );

        this.timelineRow = timeline.addPlotRow( "Example" );
        this.timelineRow.addPainter( this.timelinePainter );

        this.timeAxisListener = new TaggedAxisListener1D( )
        {
            @Override
            public void tagsUpdated( TaggedAxis1D axis )
            {
                long tMin_PMILLIS = timeline.plot.getTimeSelectionMin( ).toPosixMillis( );
                long tMax_PMILLIS = timeline.plot.getTimeSelectionMax( ).toPosixMillis( );
                long tCursor_PMILLIS = timeline.plot.getTimeSelection( ).toPosixMillis( );
                geoPainter.setTimeSelection( tMin_PMILLIS, tMax_PMILLIS, tCursor_PMILLIS );
                timelinePainter.setTimeSelection( tMin_PMILLIS, tMax_PMILLIS, tCursor_PMILLIS );
            }
        };
        timeline.plot.getTimeAxis( ).addAxisListener( this.timeAxisListener );
    }

    @Override
    public void uninstallFromTimeline( LayeredTimeline timeline, GlimpseContext context )
    {
        timeline.plot.getTimeAxis( ).removeAxisListener( this.timeAxisListener );
        this.timeAxisListener = null;

        this.timelineRow.removePainter( this.timelinePainter );
        this.timelinePainter.dispose( context );
        this.timelinePainter = null;

        timeline.plot.removePlot( this.timelineRow.getId( ) );
        this.timelineRow = null;
    }

}
