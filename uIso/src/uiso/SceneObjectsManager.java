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

import uiso.interfaces.IDrawer;

class SceneObjectsManager {
	/* Package: */
	SceneObjectsManager(UIsoEngine isometric_engine, int max_sprite_objects_in_the_scene, int max_string_objects_in_the_scene) {
		int i;

		this.isometric_engine = isometric_engine;
		this.debug = isometric_engine.debug;
		this.virtual_coordinates = isometric_engine.virtual_coordinates;
		this.real_coordinates = isometric_engine.real_coordinates;
		this.string_bounds = isometric_engine.string_bounds;
		this.virtual_world_tile_size = isometric_engine.virtual_world_tile_size;
		this.tile_max_z = isometric_engine.tile_max_z;
		this.sprites = isometric_engine.sprites;
		this.drawer = isometric_engine.drawer;
		this.viewport_w = isometric_engine.viewport_w;
		this.viewport_h = isometric_engine.viewport_h;

		this.sprite_scene_objects = new SpriteSceneObject[max_sprite_objects_in_the_scene];
		for (i = 0; i < this.sprite_scene_objects.length; i++) {
			this.sprite_scene_objects[i] = new SpriteSceneObject();
		}
		this.string_scene_objects = new StringSceneObject[max_string_objects_in_the_scene];
		for (i = 0; i < this.string_scene_objects.length; i++) {
			this.string_scene_objects[i] = new StringSceneObject();
		}
	}

	void startScene() {
		this.n_sprite_scene_objects = this.n_string_scene_objects = 0;
		this.viewport_offset_x = this.isometric_engine.viewport_offset_x;
		this.viewport_offset_y = this.isometric_engine.viewport_offset_y;
	}

	void insertObjectInScene(UIsoObject object) {
		if (!object.isSelected() && object.isVisible()) {
			this.virtual_coordinates.x = object.getX() + this.tile_max_z * this.virtual_world_tile_size;
			this.virtual_coordinates.y = object.getY() + this.tile_max_z * this.virtual_world_tile_size;
			this.virtual_coordinates.z = object.getZ();

			UIsoEngine.toRealCoordinates(this.virtual_coordinates, this.real_coordinates);
			this.real_coordinates.x -= this.viewport_offset_x;
			this.real_coordinates.y -= this.viewport_offset_y;

			if (object instanceof SpriteObject) {
				Sprite sprite;
				UIsoImage image;

				this.drawer.getObjectSprite((SpriteObject) object, this.sprites);
				sprite = this.sprites[0];
				if (sprite == null)
					return;
				image = sprite.image;

				this.real_coordinates.x -= sprite.getAnchorX();
				this.real_coordinates.y -= sprite.getAnchorY();

				/* Check the rectangles intersection. */
				if (this.real_coordinates.y + image.getH() < 0 || this.real_coordinates.y >= this.viewport_h || this.real_coordinates.x + image.getW() < 0
						|| this.real_coordinates.x >= this.viewport_w)
					return;

				if (this.n_sprite_scene_objects < this.sprite_scene_objects.length) {
					object.setSelected(true);
					this.sprite_scene_objects[this.n_sprite_scene_objects].image = image;
					this.sprite_scene_objects[this.n_sprite_scene_objects].sprite_object = (SpriteObject) object;
					this.sprite_scene_objects[this.n_sprite_scene_objects].real_coordinates.copyFrom(this.real_coordinates);
					this.prepareSpriteSceneObjectToBeCompared(this.sprite_scene_objects[this.n_sprite_scene_objects]);
					this.n_sprite_scene_objects++;
				} else if (this.debug) {
					System.err.println("[WARN] There was no sufficient space to draw this object in the scene. " + "Try to increase \"max_sprite_objects_in_the_scene\".");
				}
			} else if (object instanceof StringObject) {
				StringObject stringObject = (StringObject) object;
				this.drawer.getStringBounds(stringObject.getString(), this.string_bounds, stringObject.getFont());
				this.real_coordinates.x -= (this.string_bounds.x >> 1);
				this.real_coordinates.y -= (this.string_bounds.y >> 1);

				/* Check the rectangles intersection. */
				if (this.real_coordinates.y + this.string_bounds.y < 0 || this.real_coordinates.y >= this.viewport_h || this.real_coordinates.x + this.string_bounds.x < 0
						|| this.real_coordinates.x >= this.viewport_w)
					return;

				if (this.n_string_scene_objects < this.string_scene_objects.length) {
					object.setSelected(true);
					this.string_scene_objects[this.n_string_scene_objects].string_object = (StringObject) object;
					this.string_scene_objects[this.n_string_scene_objects].real_coordinates.copyFrom(this.real_coordinates);
					this.n_string_scene_objects++;
				} else if (this.debug) {
					System.err.println("[WARN] There was no sufficient space to draw this object in the scene. " + "Try to increase \"max_string_objects_in_the_scene\".");
				}
			}
		}
	}

	void drawSceneObjects() {
		int i;
		SpriteSceneObject sprite_scene_object;
		StringSceneObject string_scene_object;

		this.sortSceneObjects(this.n_sprite_scene_objects);
		for (i = 0; i < this.n_sprite_scene_objects; i++) {
			sprite_scene_object = this.sprite_scene_objects[i];
			sprite_scene_object.sprite_object.setSelected(false);
			this.drawer.drawImage(sprite_scene_object.real_coordinates.x, sprite_scene_object.real_coordinates.y, sprite_scene_object.image);
			if (this.debug) {
				this.drawSpriteObjectBoundingBox(sprite_scene_object);

				this.virtual_coordinates.x = sprite_scene_object.sprite_object.getX() + this.tile_max_z * this.virtual_world_tile_size;
				this.virtual_coordinates.y = sprite_scene_object.sprite_object.getY() + this.tile_max_z * this.virtual_world_tile_size;
				this.virtual_coordinates.z = sprite_scene_object.sprite_object.getZ();
				UIsoEngine.toRealCoordinates(this.virtual_coordinates, this.real_coordinates);
				this.drawer.drawString(this.real_coordinates.x - this.viewport_offset_x, this.real_coordinates.y - this.viewport_offset_y, Integer.toString(i));
			}
		}

		for (i = 0; i < this.n_string_scene_objects; i++) {
			string_scene_object = this.string_scene_objects[i];
			string_scene_object.string_object.setSelected(false);
			this.drawer.drawString(string_scene_object.real_coordinates.x, string_scene_object.real_coordinates.y, string_scene_object.string_object.getString(),
					string_scene_object.string_object.getFont(), string_scene_object.string_object.getColor());
			if (this.debug)
				this.drawStringSceneObjectBounds(string_scene_object);
		}

	}

	/* Private: */
	private boolean debug;
	private int n_sprite_scene_objects, n_string_scene_objects, virtual_world_tile_size, tile_max_z, viewport_offset_x, viewport_offset_y, viewport_w, viewport_h;
	private IDrawer drawer;
	private UIsoEngine isometric_engine;
	private StringSceneObject[] string_scene_objects;
	private SpriteSceneObject[] sprite_scene_objects;
	private Sprite[] sprites;
	private Point virtual_coordinates, real_coordinates, string_bounds;

	private void drawSpriteObjectBoundingBox(SpriteSceneObject sprite_scene_object) {
		/*
		 *           /\ <--- G 
		 *          /  \ 
		 *         /    \ 
		 *  F ---> \    / <--- C 
		 *        | \  / |
		 *        |  \/  | <--- D 
		 *        |   |  | 
		 *        |   |  | 
		 *        |   |  | 
		 *        |   |  | 
		 *        |   |  | 
		 *  E --->\  |  / <--- B 
		 *         \ | / 
		 *          \|/ <--- A (X,Y,Z)
		 */
		SpriteObject sprite_object = sprite_scene_object.sprite_object;
		int a_x, a_y, b_x, b_y, c_x, c_y, d_x, d_y, e_x, e_y, f_x, f_y, g_x, g_y, x, y, z;
		Sprite sprite;

		x = sprite_scene_object.sprite_object.getX();
		y = sprite_scene_object.sprite_object.getY();
		z = sprite_scene_object.sprite_object.getZ();

		this.drawer.getObjectSprite(sprite_object, this.sprites);
		sprite = this.sprites[0];

		this.virtual_coordinates.x = x + sprite.getBoundingBoxOffsetX() + this.tile_max_z * this.virtual_world_tile_size;
		this.virtual_coordinates.y = y + sprite.getBoundingBoxOffsetY() + this.tile_max_z * this.virtual_world_tile_size;
		this.virtual_coordinates.z = z + sprite.getBoundingBoxOffsetZ();
		UIsoEngine.toRealCoordinates(this.virtual_coordinates, this.real_coordinates);
		a_x = this.real_coordinates.x - this.viewport_offset_x;
		a_y = this.real_coordinates.y - this.viewport_offset_y;

		this.virtual_coordinates.x = x + sprite.getBoundingBoxOffsetX() + this.tile_max_z * this.virtual_world_tile_size - sprite.getBoundingBoxW();
		this.virtual_coordinates.y = y + sprite.getBoundingBoxOffsetY() + this.tile_max_z * this.virtual_world_tile_size;
		this.virtual_coordinates.z = z + sprite.getBoundingBoxOffsetZ() + sprite.getBoundingBoxL();
		UIsoEngine.toRealCoordinates(this.virtual_coordinates, this.real_coordinates);
		c_x = this.real_coordinates.x - this.viewport_offset_x;
		c_y = this.real_coordinates.y - this.viewport_offset_y;

		this.virtual_coordinates.x = x + sprite.getBoundingBoxOffsetX() + this.tile_max_z * this.virtual_world_tile_size;
		this.virtual_coordinates.y = y + sprite.getBoundingBoxOffsetY() + this.tile_max_z * this.virtual_world_tile_size;
		this.virtual_coordinates.z = z + sprite.getBoundingBoxOffsetZ() + sprite.getBoundingBoxL();
		UIsoEngine.toRealCoordinates(this.virtual_coordinates, this.real_coordinates);
		d_x = this.real_coordinates.x - this.viewport_offset_x;
		d_y = this.real_coordinates.y - this.viewport_offset_y;

		this.virtual_coordinates.x = x + sprite.getBoundingBoxOffsetX() + this.tile_max_z * this.virtual_world_tile_size;
		this.virtual_coordinates.y = y + sprite.getBoundingBoxOffsetY() + this.tile_max_z * this.virtual_world_tile_size - sprite.getBoundingBoxH();
		this.virtual_coordinates.z = z + sprite.getBoundingBoxOffsetZ();
		UIsoEngine.toRealCoordinates(this.virtual_coordinates, this.real_coordinates);
		e_x = this.real_coordinates.x - this.viewport_offset_x;
		e_y = this.real_coordinates.y - this.viewport_offset_y;

		this.virtual_coordinates.x = x + sprite.getBoundingBoxOffsetX() + this.tile_max_z * this.virtual_world_tile_size - sprite.getBoundingBoxW();
		this.virtual_coordinates.y = y + sprite.getBoundingBoxOffsetY() + this.tile_max_z * this.virtual_world_tile_size;
		this.virtual_coordinates.z = z + sprite.getBoundingBoxOffsetZ();
		UIsoEngine.toRealCoordinates(this.virtual_coordinates, this.real_coordinates);
		b_x = this.real_coordinates.x - this.viewport_offset_x;
		b_y = this.real_coordinates.y - this.viewport_offset_y;

		this.virtual_coordinates.x = x + sprite.getBoundingBoxOffsetX() + this.tile_max_z * this.virtual_world_tile_size;
		this.virtual_coordinates.y = y + sprite.getBoundingBoxOffsetY() + this.tile_max_z * this.virtual_world_tile_size - sprite.getBoundingBoxH();
		this.virtual_coordinates.z = z + sprite.getBoundingBoxOffsetZ() + sprite.getBoundingBoxL();
		UIsoEngine.toRealCoordinates(this.virtual_coordinates, this.real_coordinates);
		f_x = this.real_coordinates.x - this.viewport_offset_x;
		f_y = this.real_coordinates.y - this.viewport_offset_y;

		this.virtual_coordinates.x = x + sprite.getBoundingBoxOffsetX() + this.tile_max_z * this.virtual_world_tile_size - sprite.getBoundingBoxW();
		this.virtual_coordinates.y = y + sprite.getBoundingBoxOffsetY() + this.tile_max_z * this.virtual_world_tile_size - sprite.getBoundingBoxH();
		this.virtual_coordinates.z = z + sprite.getBoundingBoxOffsetZ() + sprite.getBoundingBoxL();
		UIsoEngine.toRealCoordinates(this.virtual_coordinates, this.real_coordinates);
		g_x = this.real_coordinates.x - this.viewport_offset_x;
		g_y = this.real_coordinates.y - this.viewport_offset_y;

		this.drawer.drawLine(a_x, a_y, b_x + 1, b_y);
		this.drawer.drawLine(a_x, a_y, d_x, d_y);
		this.drawer.drawLine(e_x, e_y, a_x + 1, a_y);
		this.drawer.drawLine(e_x, e_y, f_x, f_y);
		this.drawer.drawLine(b_x + 1, b_y, c_x + 1, c_y);
		this.drawer.drawLine(f_x, f_y, d_x + 1, d_y);
		this.drawer.drawLine(d_x, d_y, c_x + 1, c_y);
		this.drawer.drawLine(f_x, f_y, g_x + 1, g_y);
		this.drawer.drawLine(g_x, g_y, c_x + 1, c_y);
	}

	private void drawStringSceneObjectBounds(StringSceneObject string_scene_object) {
		Point real_coordinates = string_scene_object.real_coordinates;

		this.drawer.getStringBounds(string_scene_object.string_object.getString(), this.string_bounds, string_scene_object.string_object.getFont());
		this.drawer.drawLine(real_coordinates.x, real_coordinates.y, real_coordinates.x, real_coordinates.y + this.string_bounds.y);
		this.drawer.drawLine(real_coordinates.x, real_coordinates.y, real_coordinates.x + this.string_bounds.x, real_coordinates.y);
		this.drawer.drawLine(real_coordinates.x + this.string_bounds.x, real_coordinates.y + this.string_bounds.y, real_coordinates.x, real_coordinates.y
				+ this.string_bounds.y);
		this.drawer.drawLine(real_coordinates.x + this.string_bounds.x, real_coordinates.y + this.string_bounds.y, real_coordinates.x + this.string_bounds.x,
				real_coordinates.y);
	}

	/*
	 * Quicksort implementation methods:
	 */
	private void swap(int a, int b) {
		SpriteSceneObject aux = this.sprite_scene_objects[a];
		this.sprite_scene_objects[a] = this.sprite_scene_objects[b];
		this.sprite_scene_objects[b] = aux;
	}

	private int partition(int left, int right) {
		int i, j;

		i = left;
		for (j = left + 1; j <= right; j++) {
			if (this.compareSpriteSceneObject(this.sprite_scene_objects[left], this.sprite_scene_objects[j])) {
				i++;
				this.swap(i, j);
			}
		}
		this.swap(left, i);
		return i;
	}

	private void recursiveSortSceneObjects(int left, int right) {
		int r;

		if (right > left) {
			r = this.partition(left, right);
			this.recursiveSortSceneObjects(left, r - 1);
			this.recursiveSortSceneObjects(r + 1, right);
		}
	}

	private void sortSceneObjects(int n_sprite_scene_objects) {
		this.recursiveSortSceneObjects(0, n_sprite_scene_objects - 1);
	}

	/*
	 * End of quicksort implementation methods.
	 */

	private void prepareSpriteSceneObjectToBeCompared(SpriteSceneObject sprite_scene_object) {
		SpriteObject sprite_object = sprite_scene_object.sprite_object;
		Sprite sprite;

		this.drawer.getObjectSprite(sprite_object, this.sprites);
		sprite = this.sprites[0];

		sprite_scene_object.max_x = sprite_object.getX() + sprite.getBoundingBoxOffsetX();
		sprite_scene_object.min_x = sprite_scene_object.max_x - sprite.getBoundingBoxW();
		sprite_scene_object.max_y = sprite_object.getY() + sprite.getBoundingBoxOffsetY();
		sprite_scene_object.min_y = sprite_scene_object.max_y - sprite.getBoundingBoxH();
		sprite_scene_object.min_z = sprite_object.getZ() + sprite.getBoundingBoxOffsetZ();
		sprite_scene_object.max_z = sprite_scene_object.min_z + sprite.getBoundingBoxL();
	}

	/* Does "b" must be drawn before "a"? */
	/* This criterion is not perfect. Some adjustments or even a different criterion will be necessary depending on the usage. It has been inspired by OpenTTD isometric engine. */
	private boolean compareSpriteSceneObject(SpriteSceneObject a, SpriteSceneObject b) {
		boolean x_intersects = intersects(a.min_x, a.max_x, b.min_x, b.max_x);
		boolean y_intersects = intersects(a.min_y, a.max_y, b.min_y, b.max_y);
		boolean z_intersects = intersects(a.min_z, a.max_z, b.min_z, b.max_z);
		boolean all_intersects = x_intersects && y_intersects && z_intersects;

		int b_max = b.max_x, a_min = a.min_x;
		int max_diff = all_intersects || !x_intersects ? Math.abs(a.min_x - b.max_x) : 0;
		int diff_y = all_intersects || !y_intersects ? Math.abs(a.min_y - b.max_y) : 0;
		if (max_diff < diff_y) {
			max_diff = diff_y;
			b_max = b.max_y;
			a_min = a.min_y;
		}
		int diff_z = all_intersects || !z_intersects ? Math.abs(a.min_z - b.max_z) : 0;
		if (max_diff < diff_z) {
			max_diff = diff_z;
			b_max = b.max_z;
			a_min = a.min_z;
		}
		return b_max < a_min;
	}

	private static boolean intersects(int min_1, int max_1, int min_2, int max_2) {
		/* Does not intersect: max_1 < min_2 || min_1 > max_2 */
		return max_1 >= min_2 && min_1 <= max_2;
	}
}
