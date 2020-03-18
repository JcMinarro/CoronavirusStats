# Coronavirus Stats

Small scripts that create three charts with the new confirmed/death/recovered people per day.
For now it only uses some countries, but you can add/remove any one else updating `Country` enum.
All data is obtained from [2019 Novel Coronavirus COVID-19 (2019-nCoV) Data Repository](https://github.com/CSSEGISandData/COVID-19)

## See charts
You can see the charts result on the following links:

-. New confirmed cases [here](https://coronavirus-a7107.firebaseapp.com/confirmed.html)

-. New deaths [here](https://coronavirus-a7107.firebaseapp.com/death.html)

-. New recovered [here](https://coronavirus-a7107.firebaseapp.com/recovered.html)

## How to run in local
First step you need to do is update the submodule repository:
```
git submodule init
git submodule update --remote
```

It will download all needed data into `COVID-19` folder.

After that you can run the next gradle command to generate html files
```
./gradlew run --args='COVID-19/csse_covid_19_data/csse_covid_19_daily_reports/'
```
This command will generate three html documents on the `dist` directory