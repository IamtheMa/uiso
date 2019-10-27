/*
 * Copyright 2012 Luis Henrique O. Rios
 *
 * This file is part of uIsometric Engine.
 *
 * uIsometric Engine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uIsometric Engine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with uIsometric Engine.  If not, see <http://www.gnu.org/licenses/>.
 */

package uiso;

class MapingHelper {
	/* Public: */
	public MapingHelper(int tile_w, int tile_h, int virtual_world_tile_size) {
		this.a = new int[Tile.N_SLOPES][];
		this.b = new int[Tile.N_SLOPES][];

		Point[] points = new Point[4];
		for (int i = 0; i < points.length; i++) {
			points[i] = new Point();
		}
		points[POINT_N].x = tile_h;
		points[POINT_E].x = tile_w;
		points[POINT_S].x = tile_h;
		points[POINT_W].x = 0;

		for (int i = 0; i < Tile.N_SLOPES; i++) {
			int n_z, e_z, s_z, w_z;
			n_z = Tile.corner_n_z_relative_to_min_z[i];
			e_z = Tile.corner_e_z_relative_to_min_z[i];
			s_z = Tile.corner_s_z_relative_to_min_z[i];
			w_z = Tile.corner_w_z_relative_to_min_z[i];

			this.a[i] = new int[4];
			this.b[i] = new int[4];
			points[POINT_N].y = ((-n_z) * virtual_world_tile_size);
			points[POINT_E].y = ((-e_z) * virtual_world_tile_size + virtual_world_tile_size);
			points[POINT_S].y = ((-s_z) * virtual_world_tile_size + (virtual_world_tile_size << 1));
			points[POINT_W].y = ((-w_z) * virtual_world_tile_size + virtual_world_tile_size);

			for (int j = 0; j < 4; j++) {
				this.computeLineConstants(points[LINE_POINTS[j][0]], points[LINE_POINTS[j][1]], i, j);
			}
		}
	}

	/**
	 * This function gets the position of the point relative to the polygon that defines tile slope boundaries. It is not exactly and has an error of +/- 2 pixels. There
	 * are two main reasons: the approximation in the calculations that does not employ floating point numbers and the usage of equations to represent the polygon that
	 * defines tile slope boundaries.
	 * 
	 * @param point
	 *           the point coordinates relative to rectangle around tile
	 * @param slope_index
	 *           the slope index from which the polygon will be extracted
	 * @return the relative position
	 */
	public int getPositionRelativeToSlopePolygon(Point point, int slope_index) {
		int position = UIsoConstants.INSIDE_POLYGON, point_position;
		point_position = this.getPointPositionRelativeLine(point, slope_index, NE_LINE);
		if (point_position <= 0) {

			point_position = this.getPointPositionRelativeLine(point, slope_index, ES_LINE);
			if (point_position >= 0) {

				point_position = this.getPointPositionRelativeLine(point, slope_index, SW_LINE);
				if (point_position > 0) {

					point_position = this.getPointPositionRelativeLine(point, slope_index, WN_LINE);
					if (point_position > 0) {
						position = UIsoConstants.ABOVE_WN_LINE;
					}
				} else {
					position = UIsoConstants.BELOW_SW_LINE;
				}
			} else {
				position = UIsoConstants.BELOW_ES_LINE;
			}
		} else {
			position = UIsoConstants.ABOVE_NE_LINE;
		}
		return position;
	}

	public int getPointPositionRelativeLine(Point point, int slope_index, int line) {
		int a = this.a[slope_index][line], b = this.b[slope_index][line], y = 0;
		switch (a) {
			case ZERO:
				y = b;
			break;
			case ONE_HALF:
				y = b + (point.x >> 1);
			break;
			case MINUS_ONE_HALF:
				y = b - (point.x >> 1);
			break;
			case ONE:
				y = b + point.x;
			break;
			case MINUS_ONE:
				y = b - point.x;
			break;
			default:
				assert (false);
			break;
		}
		return y - point.y;
	}

	/* Private: */
	/*           
	 *              N
	 *    WN      /   \     NE
	 *          /       \
	 *       W/           \E
	 *        \           /
	 *    SW    \       /   ES
	 *            \   /
	 *              S
	 */
	private final static int POINT_N = 0;
	private final static int POINT_E = 1;
	private final static int POINT_S = 2;
	private final static int POINT_W = 3;
	private final static int NE_LINE = 0;
	private final static int ES_LINE = 1;
	private final static int SW_LINE = 2;
	private final static int WN_LINE = 3;
	private final static int ZERO = 0;
	private final static int ONE_HALF = 1;
	private final static int MINUS_ONE_HALF = 2;
	private final static int ONE = 3;
	private final static int MINUS_ONE = 4;

	private static int[][] LINE_POINTS = {{POINT_N, POINT_E}, {POINT_E, POINT_S}, {POINT_S, POINT_W}, {POINT_W, POINT_N}};

	private static int mapToInt(float number) {
		if (number == 0.0f) {
			return ZERO;
		} else if (number == 0.5f) {
			return ONE_HALF;
		} else if (number == -0.5f) {
			return MINUS_ONE_HALF;
		} else if (number == 1.0f) {
			return ONE;
		} else if (number == -1.0f) {
			return MINUS_ONE;
		} else {
			assert (false);
		}
		return ZERO;
	}

	private int[][] a, b;

	private void computeLineConstants(Point line_point_a, Point line_point_b, int slope_index, int line) {
		assert (line_point_a.x - line_point_b.x != 0);
		float a = (float) (line_point_a.y - line_point_b.y) / (float) (line_point_a.x - line_point_b.x);
		float b = (line_point_b.y) - (a * (line_point_b.x));
		assert (b - ((int) b) == 0);
		this.a[slope_index][line] = mapToInt(a);
		this.b[slope_index][line] = (int) b;
	}
}