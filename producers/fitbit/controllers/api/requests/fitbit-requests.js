var querystring = require('querystring');
var request = require('request');

function FitbitRequest(config){

    this.token = config.token;
    this.path = 'https://api.fitbit.com/1/user/-/';
    this.callType = {
        'sleep': 'sleep/date/',
        'activities': 'activities/date/',
        'friends':'friends',
        'devices':'devices',
        'food': 'foods/log/date/',
        'heart': 'activities/heart/date/',
        'profile': 'profile'
    }
}

FitbitRequest.prototype = {
    constructor: FitbitRequest,

    urlConstructor:function(type, date){
        switch(type){
            case 'devices':
                return this.path + this.callType[type] + '.json';
            case 'friends':
                return this.path + this.callType[type] + '.json';
            case 'profile':
                return this.path + this.callType[type] + '.json';
            case 'heart':
                return this.path + this.callType[type] + date + '/1d/1min.json';
            default:
                return this.path + this.callType[type] + date + '.json';
        }
    },

    getData:function(type, date, callback){
        var options = {
            url: this.urlConstructor(type, date),
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + this.token.access_token
            }
        };
        request(options, callback);
    }
}

module.exports = FitbitRequest;
