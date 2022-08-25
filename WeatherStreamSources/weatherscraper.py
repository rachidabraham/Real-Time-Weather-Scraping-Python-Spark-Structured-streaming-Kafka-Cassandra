from time import sleep
from kafka import KafkaProducer
from requests_html import HTMLSession
from datetime import datetime

################################################################
#      SCRAPING WEATHER DATA FROM GOOGLE                       #
################################################################
# Function : Scraping Google Weather Data
def get_weather_info(query, session):
    url = f'https://www.google.com/search?q=weather+{query}'
    resp = session.get(url, headers={'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko)Chrome/98.0.4758.102 Safari/537.36'})
    # Retrieving some infos
    location = resp.html.find('div#wob_loc.wob_loc', first=True).text # Region to check
    temp = resp.html.find('span#wob_tm.wob_t', first=True).text # Temperature
    unit = resp.html.find('div.vk_bk.wob-unit span.wob_t', first=True).text # Unit : °C or °F
    desc = resp.html.find('div#wob_dcp.wob_dcp span#wob_dc', first=True).text # Description : Sunny, Cloudy, etc.
    date = resp.html.find('div#wob_dts.wob_dts', first=True).text # Day
    current_datetime = datetime.now()
    # Formating data (event) and returning it
    msg = f'{location}, {temp}{unit}, {desc}, {date}, {current_datetime}'
    return msg

# HTML Session
ses = HTMLSession()

queries = ['Dakar', 'Yeumbeul', 'Keur Massar', 'Louga', 'Thies', 'Saint Louis', 'Pikine', 'Kolda'] # This can be used as ARGS

################################################################
#      PRODUCER - SENDING SCRAPED DATA TO KAFKA                #
################################################################
# Kafka Producer
producer = KafkaProducer(bootstrap_servers='localhost:9092') # localhost:9092, edgenode:6667

while True:
    for query in queries:
        message = get_weather_info(query, ses)
            
        # Sending event to the topic WEATHER
        producer.send('weather', bytes(message, encoding='utf8'))
        print(f'Sending data to kafka : {message}')
        
        sleep(0.5)
