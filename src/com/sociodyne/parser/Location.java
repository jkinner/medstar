// Copyright 2011, Sociodyne LLC. All rights reserved.
package com.sociodyne.parser;

public class Location {

  int line;
  int character;

  public Location() {
  }

  public void nextLine() {
    line++;
    character = 0;
  }

  public void nextChar() {
    character++;
  }

  public int getLine() {
    return line;
  }

  public int getChar() {
    return character;
  }

  @Override
  public String toString() {
    final StringBuffer locationBuffer = new StringBuffer();
    locationBuffer.append("line ").append(line).append(", character ").append(character);
    return locationBuffer.toString();
  }

  public static Location copyOf(Location location) {
    final Location newLocation = new Location();
    newLocation.character = location.character;
    newLocation.line = location.line;
    return newLocation;
  }
}
