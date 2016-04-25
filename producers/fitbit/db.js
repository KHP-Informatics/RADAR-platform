var mongoose = require('mongoose')
mongoose.connect('mongodb://localhost/sleepsightrescue', function(){
    console.log('MongoDB connected.')
})

module.exports = mongoose
