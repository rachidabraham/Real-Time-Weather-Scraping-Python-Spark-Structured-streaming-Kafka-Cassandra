package sn.rachitech.weather

/**
 * WeatherData case class to parse/handle scraped weather data
 */
case class WeatherData(location: String, temperature: String, description: String, day: String, loaddate: String)