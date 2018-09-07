# record-parser

Simplified example for record parsing and processing. Will eventually include a REST API.

## What it does

Loads records from 3 different file formats, sorts it in a certain way, and outputs result.

### Possible improvements

When unsure, I interpreted the problem as written. Typically though,
it would be better to confirm these with the user. I'm capturing a few of those here.

#### Format for variant CSV files
Typically, these files are delimited by a single character, with no spaces surrounding it.
So, rather than having ", " or " | " be a delimiter, one would usually do it as "," or "|".
A record would then look as follows:
```
Doe,John,male,pink,1/1/1900
```
The easiest way to clear this up would be to get an example of a source file. For now,
I assumed that the delimiter was as written, with the spaces included.

#### Format for dates
The desired display date was listed as M/D/YYYY.
If that was meant to be a a technical specification, then it could mean that
the twenty-ninth day of August in the year 2018 would be written as "8/242/2018". 
(D refers to day-of-year, rather than day-of-month.)
(At least if [you are using this](https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html) as a guide.)
I've rarely seen that format used; at least in the U.S., it would typically be written
as "8/29/2018" (which is M/d/YYYY).
(On the other hand, if we get to pick how dates are serialized as strings,
I'd recommend an ISO date; specifically, as "2018-08-29". We can still display
them however the user wants.)

Again, any display format is perfectly fine; this is just a question that I'd follow
up with the users to make sure that it is displayed how they want it. For now,
I went with it as written, which would be the first format. I also assumed that all
dates would be entered in this format.

##### Sorting by dates
On a similar note, I sorted the dates by value, rather than lexicographically.
I think this is a fairly safe assumption, but I'm pointing that out just in case.

#### Validity of input assumptions
Input from files was assumed to be valid (5 fields max, dates parseable),
and was parsed as such, without additional checks.

Planning to perform some validation to the REST interface inputs.
(If a record is malformed, it won't be added.)

### Space vs Time tradeoff

Right now, each sort takes O(N*logN) operations to finish.
If you wanted, you could use ~3x the space, and just store the data 3 times,
sorted each way. You could do this by using sorted maps, with the key for a given entry
being whatever you wanted to sort by.

This would be fairly easy to implement, but I decided against it for a few reasons.
1. It's a little more complex, and uses 3x the space.
2. Since this application isn't using a database, everything has to fit in memory anyway,
   so it is relatively small, and the N*logN cost won't be that impactful.
3. If we decide to get fancy with how the data is stored, I'd just use a proper database.

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running
For now, this will suffice:

    lein run

When the REST API is finished, and you are ready
to start a web server for the application, run:

    lein ring server

## License
 [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html)
