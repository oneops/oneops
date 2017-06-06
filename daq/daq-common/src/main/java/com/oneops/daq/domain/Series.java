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

/**
 * The Class Series.
 */
public class Series {
	private String name;
	
	private String datasource;
	// area, areaspline, bar, column, line, pie, scatter, spline, candlestick or ohlc
	private String type;
	private String xAxisId;
	private String yAxisId;
	private String stackGroup;
	// to realign week-over-week etc
	private int offset;

	private String color;
	private String weight;
	private String renderer;

	
	/**
	 * Gets the color.
	 *
	 * @return the color
	 */
	public String getColor() {
		return color;
	}
	
	/**
	 * Sets the color.
	 *
	 * @param color the new color
	 */
	public void setColor(String color) {
		this.color = color;
	}
	
	/**
	 * Gets the weight.
	 *
	 * @return the weight
	 */
	public String getWeight() {
		return weight;
	}
	
	/**
	 * Sets the weight.
	 *
	 * @param weight the new weight
	 */
	public void setWeight(String weight) {
		this.weight = weight;
	}
	
	/**
	 * Gets the renderer.
	 *
	 * @return the renderer
	 */
	public String getRenderer() {
		return renderer;
	}
	
	/**
	 * Sets the renderer.
	 *
	 * @param renderer the new renderer
	 */
	public void setRenderer(String renderer) {
		this.renderer = renderer;
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
	 * Gets the datasource.
	 *
	 * @return the datasource
	 */
	public String getDatasource() {
		return datasource;
	}
	
	/**
	 * Sets the datasource.
	 *
	 * @param datasource the new datasource
	 */
	public void setDatasource(String datasource) {
		this.datasource = datasource;
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
	 * Gets the x axis id.
	 *
	 * @return the x axis id
	 */
	public String getxAxisId() {
		return xAxisId;
	}
	
	/**
	 * Sets the x axis id.
	 *
	 * @param xAxisId the new x axis id
	 */
	public void setxAxisId(String xAxisId) {
		this.xAxisId = xAxisId;
	}
	
	/**
	 * Gets the y axis id.
	 *
	 * @return the y axis id
	 */
	public String getyAxisId() {
		return yAxisId;
	}
	
	/**
	 * Sets the y axis id.
	 *
	 * @param yAxisId the new y axis id
	 */
	public void setyAxisId(String yAxisId) {
		this.yAxisId = yAxisId;
	}
	
	/**
	 * Gets the stack group.
	 *
	 * @return the stack group
	 */
	public String getStackGroup() {
		return stackGroup;
	}
	
	/**
	 * Sets the stack group.
	 *
	 * @param stackGroup the new stack group
	 */
	public void setStackGroup(String stackGroup) {
		this.stackGroup = stackGroup;
	}
	
	/**
	 * Gets the offset.
	 *
	 * @return the offset
	 */
	public int getOffset() {
		return offset;
	}
	
	/**
	 * Sets the offset.
	 *
	 * @param offset the new offset
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}
}
