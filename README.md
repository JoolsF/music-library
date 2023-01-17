# Music Library

Gathers metadata about your listening habits and presents you with a report. As well as showing you which tracks you've 
listened to, how frequently, and when, it can show you which albums you've listened to. You can imagine a list of albums 
ordered by track plays over time, fed from Spotify, Youtube, Amazon music etc.  This is intended for people who want to 
be able to visualise what they listen to in terms of a record collection.


## Currently Supported
* Spotify

## RUN LOCALLY
* docker compose up
* pass following into run config: ```DATABASE_PASSWORD=test-password;DATABASE_SCHEMA=test-schema;DATABASE_URL=jdbc:postgresql://localhost:5432/test-db;DATABASE_USER=test-username```
* run ```curl --location --request POST 'localhost:9000/test/foo' \
  --header 'Content-Type: application/json' \
  --data-raw '{
  "id":"1",
  "bar":"test value"
  }'```
* docker compose down
