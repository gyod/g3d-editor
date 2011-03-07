package g3deditor.geo.blocks;

import g3deditor.geo.GeoBlock;
import g3deditor.geo.GeoCell;
import g3deditor.geo.GeoEngine;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.inc.incolution.util.io.IncBufferedFileWriter;

/**
 * Complex block, 1 level, 64 heights (each cell in block).
 *
 * @author Forsaiken
 */
public final class GeoBlockComplex extends GeoBlock
{
	private static final int indexOf(final int x, final int y)
	{
		return x * GeoEngine.GEO_BLOCK_SHIFT + y;
	}
	
	private final GeoCell[] _cells;
	
	public GeoBlockComplex(final ByteBuffer bb, final int geoX, final int geoY, final boolean l2j)
	{
		super(geoX, geoY);
		_cells = new GeoCell[GeoEngine.GEO_BLOCK_SHIFT * GeoEngine.GEO_BLOCK_SHIFT];
		for (int x = 0, y; x < GeoEngine.GEO_BLOCK_SHIFT; x++)
		{
			for (y = 0; y < GeoEngine.GEO_BLOCK_SHIFT; y++)
			{
				_cells[indexOf(x, y)] = new GeoCell(this, bb.getShort(), x, y);
			}
		}
		calcMaxMinHeight();
	}
	
	public GeoBlockComplex(final GeoBlockFlat block)
	{
		super(block.getGeoX(), block.getGeoY());
		
		_cells = new GeoCell[GeoEngine.GEO_BLOCK_SHIFT * GeoEngine.GEO_BLOCK_SHIFT];
		for (int x = 0, y; x < GeoEngine.GEO_BLOCK_SHIFT; x++)
		{
			for (y = 0; y < GeoEngine.GEO_BLOCK_SHIFT; y++)
			{
				_cells[indexOf(x, y)] = new GeoCell(this, GeoEngine.convertHeightToHeightAndNSWEALL(block.getMinHeight()), x, y);
			}
		}
		calcMaxMinHeight();
	}
	
	public GeoBlockComplex(final GeoBlockMultiLevel block)
	{
		super(block.getGeoX(), block.getGeoY());
		
		_cells = new GeoCell[GeoEngine.GEO_BLOCK_SHIFT * GeoEngine.GEO_BLOCK_SHIFT];
		for (int x = 0, y; x < GeoEngine.GEO_BLOCK_SHIFT; x++)
		{
			for (y = 0; y < GeoEngine.GEO_BLOCK_SHIFT; y++)
			{
				_cells[indexOf(x, y)] = new GeoCell(this, block.nGetCellByLayer(x, y, block.nGetLayerCount(x, y) - 1).getHeightAndNSWE(), x, y);
			}
		}
		calcMaxMinHeight();
	}
	
	private GeoBlockComplex(final GeoBlockComplex block)
	{
		super(block.getGeoX(), block.getGeoY());
		_cells = new GeoCell[GeoEngine.GEO_BLOCK_SHIFT * GeoEngine.GEO_BLOCK_SHIFT];
	}
	
	@Override
	public final byte getType()
	{
		return GeoEngine.GEO_BLOCK_TYPE_COMPLEX;
	}
	
	@Override
	public final int nGetLayer(final int geoX, final int geoY, final int z)
	{
		return 0;
	}
	
	@Override
	public final GeoCell nGetCellByLayer(final int geoX, final int geoY, final int layer)
	{
		final int cellX = GeoEngine.getCellXY(geoX);
		final int cellY = GeoEngine.getCellXY(geoY);
		return _cells[indexOf(cellX, cellY)];
	}
	
	@Override
	public final int nGetLayerCount(final int geoX, final int geoY)
	{
		return 1;
	}

	@Override
	public final GeoCell[] nGetLayers(final int geoX, final int geoY)
	{
		final int cellX = GeoEngine.getCellXY(geoX);
		final int cellY = GeoEngine.getCellXY(geoY);
		return new GeoCell[]{_cells[indexOf(cellX, cellY)]};
	}
	
	@Override
	public final void calcMaxMinHeight()
	{
		short minHeight = Short.MAX_VALUE, maxHeight = Short.MIN_VALUE;
		for (int x = 0, y; x < GeoEngine.GEO_BLOCK_SHIFT; x++)
		{
			for (y = 0; y < GeoEngine.GEO_BLOCK_SHIFT; y++)
			{
				final GeoCell cell = _cells[indexOf(x, y)];
				minHeight = (short) Math.min(cell.getHeight(), minHeight);
				maxHeight = (short) Math.max(cell.getHeight(), maxHeight);
			}
		}
		_minHeight = minHeight;
		_maxHeight = maxHeight;
	}
	
	@Override
	public final GeoBlockComplex clone()
	{
		final GeoBlockComplex clone = new GeoBlockComplex(this);
		copyDataTo(clone);
		return clone;
	}
	
	public final void copyDataTo(final GeoBlockComplex block)
	{
		for (int x = GeoEngine.GEO_BLOCK_SHIFT, y, i; x-- > 0;)
		{
			for (y = GeoEngine.GEO_BLOCK_SHIFT; y-- > 0;)
			{
				i = indexOf(x, y);
				block._cells[i].setHeightAndNSWE(_cells[i].getHeightAndNSWE());
			}
		}
		block._maxHeight = _maxHeight;
		block._minHeight = _minHeight;
	}

	@Override
	public final void saveTo(final IncBufferedFileWriter writer, final boolean l2j) throws IOException
	{
		writer.writeByte(GeoEngine.GEO_BLOCK_TYPE_COMPLEX);
		for (int x = 0, y; x < GeoEngine.GEO_BLOCK_SHIFT; x++)
		{
			for (y = 0; y < GeoEngine.GEO_BLOCK_SHIFT; y++)
			{
				writer.writeShort(_cells[indexOf(x, y)].getHeightAndNSWE());
			}
		}
	}
	
	@Override
	public final int getMaxLayerCount()
	{
		return 1;
	}
	
	@Override
	public final int addLayer(final int geoX, final int geoY, final short heightAndNSWE)
	{
		return -1;
	}
	
	@Override
	public final int removeLayer(final int geoX, final int geoY, final int layer)
	{
		return -1;
	}
	
	/**
	 * @see g3deditor.content.geo.GeoBlock#getAllCells()
	 */
	@Override
	public final GeoCell[] getCells()
	{
		return _cells;
	}
}