$("#lectureName").autocomplete({
    source: function (request, response) {
        $.ajax({
            url: "/evaluation/search/lecturenames",
            dataType: "json",
            data: {term: request.term},
            success: function(data) {
                response( $.map(data, function(item) {
                    return { label: item, value: item }
                }));
            },
            beforeSend:function(){
                $("#loading-indicator").show();
            },
            complete:function(){
                $("#loading-indicator").hide();
            }
        });
    },
    //한글 입력후에 방향키 이동시에 발생하는 문제 Fix
    focus: function(event,ui){
        return false;
    },
    minLength: 2
});

$("#professorName").autocomplete({
    source: function (request, response) {
        $.ajax({
            url: "/evaluation/search/professornames",
            dataType: "json",
            data: {term: request.term},
            success: function(data) {
                response( $.map(data, function(item) {
                    return { label: item, value: item }
                }));
            },
            beforeSend:function(){
                $("#loading-indicator").show();
            },
            complete:function(){
                $("#loading-indicator").hide();
            }
        });
    },
    //한글 입력후에 방향키 이동시에 발생하는 문제 Fix
    focus: function(event,ui){
        return false;
    },
    minLength: 1
});