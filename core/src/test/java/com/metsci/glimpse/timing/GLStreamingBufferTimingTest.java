package com.metsci.glimpse.timing;

import static com.metsci.glimpse.support.FrameUtils.*;
import static javax.media.opengl.GL.*;
import static javax.swing.WindowConstants.*;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAnimatorControl;
import javax.media.opengl.GLProfile;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLStreamingBuffer;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.plot.EmptyPlot2D;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;
import com.metsci.glimpse.support.shader.triangle.FlatColorProgram;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.support.swing.SwingEDTAnimator;


public class GLStreamingBufferTimingTest
{

    public static void main( String[] args )
    {
        final EmptyPlot2D plot = new EmptyPlot2D( );
        plot.addPainter( new BackgroundPainter( ) );
        plot.addPainter( new TestPainter( ) );
        plot.addPainter( new FpsPrinter( ) );

        SwingUtilities.invokeLater( new Runnable( )
        {
            public void run( )
            {
                NewtSwingEDTGlimpseCanvas canvas = new NewtSwingEDTGlimpseCanvas( GLProfile.GL3 );
                canvas.addLayout( plot );
                canvas.setLookAndFeel( new SwingLookAndFeel( ) );

                GLAnimatorControl animator = new SwingEDTAnimator( 1000 );
                animator.add( canvas.getGLDrawable( ) );
                animator.start( );

                JFrame frame = newFrame( "GLStreamingBufferTimingTest", canvas, DISPOSE_ON_CLOSE );
                stopOnWindowClosing( frame, animator );
                disposeOnWindowClosing( frame, canvas );
                showFrameCentered( frame );
            }
        } );
    }

    protected static class TestPainter extends GlimpsePainterBase
    {
        protected static final int numIterations = 10000;
        protected static final int verticesPerIteration = 4;
        protected static final int floatsPerIteration = 2 * verticesPerIteration;

        protected final FlatColorProgram prog;
        protected final GLStreamingBuffer buffer;

        public TestPainter( )
        {
            this.prog = new FlatColorProgram( );
            this.buffer = new GLStreamingBuffer( GL_DYNAMIC_DRAW, 5*numIterations );
        }

        @Override
        public void doPaintTo( GlimpseContext context )
        {
            GL3 gl = context.getGL( ).getGL3( );
            GlimpseBounds bounds = getBounds( context );

            this.prog.begin( gl );
            this.prog.setColor( gl, 0, 0, 0, 1 );
            this.prog.setPixelOrtho( gl, bounds );

            for ( int i = 0; i < numIterations; i++ )
            {
                FloatBuffer mappedFloats = this.buffer.mapFloats( gl, floatsPerIteration );

                for ( int v = 0; v < verticesPerIteration; v++ )
                {
                    mappedFloats.put( v ).put( v );
                }

                this.buffer.seal( gl );

                int b = this.buffer.buffer( gl );
                int n = verticesPerIteration;
                this.prog.draw( gl, GL_POINTS, b, 0, n );
            }

            this.prog.end( gl );
        }

        @Override
        protected void doDispose( GlimpseContext context )
        {
            GL gl = context.getGL( );
            this.buffer.dispose( gl );
        }
    }

}
