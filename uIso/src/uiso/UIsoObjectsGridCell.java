/*
 * Copyright 2012, 2015 Luis Henrique O. Rios
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

import java.util.ArrayList;

class UIsoObjectsGridCell extends LinkedListElement {
	/* Package: */
	UIsoObject isometric_engine_object;
	ArrayList<UIsoObject> objects = new ArrayList<UIsoObject>();

	void insertObject(UIsoObject object, int vertex) {
		UIsoObject previous_first_object = this.isometric_engine_object;

		this.isometric_engine_object = object;
		object.setValueToVertex(this, previous_first_object, vertex);
		objects.add(object);
		if (previous_first_object == null)
			return;

		int aux = previous_first_object.getVertexFromPreviousElement(this);

		previous_first_object.setValueToPreviousVertexField(object, aux);
		previous_first_object.setVertexOfPreviousElementThatContinuesTheListInVertex(vertex, aux);
		object.setVertexOfNextElementThatContinuesTheListInVertex(aux, vertex);
	}
	public UIsoObject findObject(int x, int y){
		if(objects.size() <= 0){
			return null;
		}
		System.out.println("objects " + objects.size());
		for(int i = 0; i < objects.size(); i++){
			if(objects.get(i).getX() <= (x*16) + 16 && objects.get(i).getX() >= (x*16) - 16
			&& objects.get(i).getY() <= (y*16) + 15 && objects.get(i).getY() >= (y*16) - 16){
				System.out.println("example " + i + " " + objects.get(i) + " " + x + "," + y);
				return objects.get(i);
			}
		}
		return null;
	}
	void removeObject(UIsoObject object) {
		int index = objects.indexOf(object);
		objects.remove(index);
	}
}