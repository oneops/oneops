/*******************************************************************************
 *  
 *   Copyright 2015 Walmart, Inc.
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  
 *******************************************************************************/
package com.oneops.daq.domain;


import java.util.Map;

/**
 * The Class Chart.
 */
public class Chart {
	private String name;
	private String description;
	private String creator;
	private String title;
	private String type;
	private String ymin;
	private String ymax;
	private String theme;
	private String step;
	private String height;
	private String width;
	private String created;
	private String updated;
	private String start;
	private String end;	
	private String key;
	private Map<String,Series> series;

	/**
	 * Gets the key.
	 *
	 * @return the key
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * Sets the key.
	 *
	 * @param key the new key
	 */
	public void setKey(String key) {
		this.key = key;
	}
	
	/**
	 * Gets the step.
	 *
	 * @return the step
	 */
	public String getStep() {
		return step;
	}
	
	/**
	 * Sets the step.
	 *
	 * @param step the new step
	 */
	public void setStep(String step) {
		this.step = step;
	}
	
	/**
	 * Gets the updated.
	 *
	 * @return the updated
	 */
	public String getUpdated() {
		return updated;
	}
	
	/**
	 * Sets the updated.
	 *
	 * @param updated the new updated
	 */
	public void setUpdated(String updated) {
		this.updated = updated;
	}	
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Gets the title.
	 *
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Sets the title.
	 *
	 * @param title the new title
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Sets the type.
	 *
	 * @param type the new type
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * Gets the ymin.
	 *
	 * @return the ymin
	 */
	public String getYmin() {
		return ymin;
	}
	
	/**
	 * Sets the ymin.
	 *
	 * @param ymin the new ymin
	 */
	public void setYmin(String ymin) {
		this.ymin = ymin;
	}
	
	/**
	 * Gets the ymax.
	 *
	 * @return the ymax
	 */
	public String getYmax() {
		return ymax;
	}
	
	/**
	 * Sets the ymax.
	 *
	 * @param ymax the new ymax
	 */
	public void setYmax(String ymax) {
		this.ymax = ymax;
	}
	
	/**
	 * Gets the height.
	 *
	 * @return the height
	 */
	public String getHeight() {
		return height;
	}
	
	/**
	 * Sets the height.
	 *
	 * @param height the new height
	 */
	public void setHeight(String height) {
		this.height = height;
	}
	
	/**
	 * Gets the width.
	 *
	 * @return the width
	 */
	public String getWidth() {
		return width;
	}
	
	/**
	 * Sets the width.
	 *
	 * @param width the new width
	 */
	public void setWidth(String width) {
		this.width = width;
	}
	
	/**
	 * Gets the theme.
	 *
	 * @return the theme
	 */
	public String getTheme() {
		return theme;
	}
	
	/**
	 * Sets the theme.
	 *
	 * @param theme the new theme
	 */
	public void setTheme(String theme) {
		this.theme = theme;
	}
	
	/**
	 * Gets the series.
	 *
	 * @return the series
	 */
	public Map<String,Series> getSeries() {
		return series;
	}
	
	/**
	 * Sets the series map.
	 *
	 * @param series the series
	 */
	public void setSeriesMap(Map<String,Series> series) {
		this.series = series;
	}
	
	/**
	 * Gets the start.
	 *
	 * @return the start
	 */
	public String getStart() {
		return start;
	}
	
	/**
	 * Sets the start.
	 *
	 * @param start the new start
	 */
	public void setStart(String start) {
		this.start = start;
	}
	
	/**
	 * Gets the end.
	 *
	 * @return the end
	 */
	public String getEnd() {
		return end;
	}
	
	/**
	 * Sets the end.
	 *
	 * @param end the new end
	 */
	public void setEnd(String end) {
		this.end = end;
	}
	
	/**
	 * Gets the creator.
	 *
	 * @return the creator
	 */
	public String getCreator() {
		return creator;
	}
	
	/**
	 * Sets the creator.
	 *
	 * @param creator the new creator
	 */
	public void setCreator(String creator) {
		this.creator = creator;
	}

	/**
	 * Gets the created.
	 *
	 * @return the created
	 */
	public String getCreated() {
		return created;
	}

	/**
	 * Sets the created.
	 *
	 * @param created the new created
	 */
	public void setCreated(String created) {
		this.created = created;
	}
}
