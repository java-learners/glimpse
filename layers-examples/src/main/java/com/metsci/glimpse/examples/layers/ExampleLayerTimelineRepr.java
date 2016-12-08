package com.metsci.glimpse.examples.layers;

import static com.metsci.glimpse.layers.AxisUtils.addAxisListener2D;
import static com.metsci.glimpse.layers.AxisUtils.addTaggedAxisListener1D;
import static com.metsci.glimpse.layers.geo.LayeredGeoConfig.requireGeoConfig;
import static com.metsci.glimpse.layers.timeline.LayeredTimelineConfig.requireTimelineConfig;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.AxisListener2D;
import com.metsci.glimpse.axis.tagged.TaggedAxisListener1D;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.layers.LayerRepr;
import com.metsci.glimpse.layers.geo.LayeredGeoConfig;
import com.metsci.glimpse.layers.timeline.LayeredTimeline;
import com.metsci.glimpse.layers.timeline.LayeredTimelineConfig;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.vector.Vector2d;

public class ExampleLayerTimelineRepr implements LayerRepr
{

    protected final LayeredTimeline view;
    protected final LayeredGeoConfig geoConfig;
    protected final LayeredTimelineConfig timelineConfig;

    protected final TimePlotInfo row;
    protected final ExampleTimelinePainter painter;

    protected final AxisListener2D geoAxisListener;
    protected final TaggedAxisListener1D timeAxisListener;


    public ExampleLayerTimelineRepr( LayeredTimeline view, ExampleStyle style )
    {
        this.view = view;
        this.geoConfig = requireGeoConfig( this.view );
        this.timelineConfig = requireTimelineConfig( this.view );

        // By using a fixed rowId, we share the row with other layers that use the same rowId
        String rowId = "z_SU";
        this.row = view.acquirePlotRow( rowId, "Example" );

        this.painter = new ExampleTimelinePainter( style );
        this.row.addPainter( this.painter );

        this.geoAxisListener = addAxisListener2D( this.geoConfig.axis, true, ( axis ) ->
        {
            Axis1D xAxis = this.geoConfig.axis.getAxisX( );
            Axis1D yAxis = this.geoConfig.axis.getAxisY( );

            float xMin = ( float ) ( xAxis.getSelectionCenter( ) - 0.5*xAxis.getSelectionSize( ) );
            float xMax = ( float ) ( xAxis.getSelectionCenter( ) + 0.5*xAxis.getSelectionSize( ) );
            float yMin = ( float ) ( yAxis.getSelectionCenter( ) - 0.5*yAxis.getSelectionSize( ) );
            float yMax = ( float ) ( yAxis.getSelectionCenter( ) + 0.5*yAxis.getSelectionSize( ) );

            this.painter.setXyWindow( xMin, xMax, yMin, yMax );
        } );

        this.timeAxisListener = addTaggedAxisListener1D( this.timelineConfig.axis, true, ( axis ) ->
        {
            Epoch epoch = this.timelineConfig.epoch;

            float tMin = ( float ) epoch.fromPosixMillis( this.timelineConfig.selectionMin_PMILLIS( ) );
            float tMax = ( float ) epoch.fromPosixMillis( this.timelineConfig.selectionMax_PMILLIS( ) );

            this.painter.setTWindow( tMin, tMax );
        } );
    }

    public void addPoint( ExamplePoint point )
    {
        Epoch epoch = this.timelineConfig.epoch;
        float t = ( float ) epoch.fromPosixMillis( point.time_PMILLIS );

        GeoProjection geoProj = this.geoConfig.proj;
        Vector2d xy_SU = geoProj.project( point.latlon );
        float x = ( float ) xy_SU.getX( );
        float y = ( float ) xy_SU.getY( );

        float z = ( float ) point.z_SU;

        this.painter.addPoint( t, x, y, z );
    }

    @Override
    public void setVisible( boolean visible )
    {
        this.painter.setVisible( visible );
    }

    @Override
    public void dispose( boolean reinstalling )
    {
        this.timelineConfig.axis.removeAxisListener( this.timeAxisListener );
        this.geoConfig.axis.removeAxisListener( this.geoAxisListener );

        this.row.removePainter( this.painter );
        this.view.releaseRow( this.row.getId( ), reinstalling );

        this.view.canvas.getGLDrawable( ).invoke( true, ( glDrawable ) ->
        {
            GlimpseContext context = this.view.canvas.getGlimpseContext( );
            this.painter.dispose( context );
            return false;
        } );
    }

}
