xquery version "3.0";

module namespace utils='eXmin/utils';

declare function utils:percent($current, $max) {
    (number($current) div number($max)) * 100
};

declare function utils:msecsToDHM($msecs) {
    if ($msecs) then
        let $secs := $msecs idiv 1000
	    let $mins := $secs idiv 60
	    let $secs := $secs - ($mins * 60)
	    let $hours := $mins idiv 60
	    let $mins := $mins - ($hours * 60)
	    let $days := $hours idiv 24
	    let $hours := $hours - ($days * 24)

    	return
        	concat($days," days ",$hours,":",$mins)
    else
        ""
};

declare function utils:format-part($function, $name, $duration) {
    let $suffix := 
        if ($name) then 
            if (string-length($name) eq 1) then 
                concat($name, " ") 
            else 
                concat(" ", $name, " ") 
        else 
            ":"
    return
        if ($function($duration)) then 
            concat(round($function($duration)), $suffix) 
        else 
            ""
};

declare function utils:durationToDHM($duration) {
    if ($duration eq ()) then
        ""
    else
        concat(
            utils:format-part(years-from-duration#1, "year", $duration),
            utils:format-part(months-from-duration#1, "month", $duration),
            utils:format-part(days-from-duration#1, "day", $duration),
            utils:format-part(hours-from-duration#1, "h", $duration),
            utils:format-part(minutes-from-duration#1, "m", $duration),
            utils:format-part(seconds-from-duration#1, "s", $duration)
        )
};