Stupid jQuery Table Sort
========================

This is a stupid jQuery table sorting plugin. Nothing fancy, nothing really
impressive. Overall, stupidly simple. Requires jQuery 1.7 or newer.

[View the demo here][0]

See the example.html document to see how to implement it.


Example Usage
-------------

The JS:

    $("table").stupidtable();

The HTML:

    <table>
      <thead>
        <tr>
          <th data-sort="int">int</th>
          <th data-sort="float">float</th>
          <th data-sort="string">string</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td>15</td>
          <td>-.18</td>
          <td>banana</td>
        </tr>
        ...
        ...
        ...

The thead and tbody tags must be used.

Add a `data-sort` attribute of "DATATYPE" to the th elements to make them sortable
by that data type. If you don't want that column to be sortable, just omit the
`data-sort` attribute.


Predefined data types
---------------------

Our aim is to keep this plugin as lightweight as possible. Consequently, the
only predefined datatypes that you can pass to the th elements are

* `int`
* `float`
* `string` (case-sensitive)
* `string-ins` (case-insensitive)

These data types will be sufficient for many simple tables. However, if you need
different data types for sorting, you can easily create your own!

Data with multiple representations/predefined order
---------------------------------------------------

Stupid Table lets you sort a column by computer friendly values while displaying
human friendly values via the `data-sort-value` attribute on a td element. For
example, to sort timestamps (computer friendly) but display pretty formated
dates (human friendly)

    <table>
      <thead>
        <tr>
          <th data-sort="string">Name</th>
          <th data-sort="int">Birthday</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td>Joe McCullough</td>
          <td data-sort-value="672537600">April 25, 1991</td>
        </tr>
        <tr>
          <td>Clint Dempsey</td>
          <td data-sort-value="416016000">March 9, 1983</td>
        </tr>
        ...
        ...
        ...

In this example, Stupid Table will sort the Birthday column by the timestamps
provided in the `data-sort-value` attributes of the corresponding tds. Since
timestamps are integers, and that's what we're sorting the column by, we specify
the Birthday column as an `int` column in the `data-sort` value of the column
header.


Default sorting direction
-------------------------

By default, columns will sort ascending. You can specify a column to sort "asc"
or "desc" first.

    <table>
      <thead>
        <tr>
            <th data-sort="float" data-sort-default="desc">float</th>
            ...
        </tr>
      </thead>
    </table>

Sorting a column programatically
--------------------------------

After you have called `$("#mytable").stupidtable()`, if you wish to sort a
column without requiring the user to click on it, select the column th and call


    var $table = $("#mytable").stupidtable();
    var $th_to_sort = $table.find("thead th").eq(0);
    $th_to_sort.stupidsort();

    // You can also force a direction.
    $th_to_sort.stupidsort('asc');
    $th_to_sort.stupidsort('desc');

Updating a table cell's value
-----------------------------

If you wish for Stupid Table to respond to changes in the table cell values, you
must explicitely inform Stupid Table to update its cache with the new values. If
you update the table display/sort values without using this mechanism, your
newly updated table **will not sort correctly!**

    /*
     * Suppose $age_td is some td in a table under a column specified as an int
     * column. stupidtable() must already be called for this table.
     */
    $age_td.updateSortVal(23);

Note that this only changes the internal sort value (whether you specified a
`data-sort-value` or not). Use the standard jQuery `.text()` / `.html()` methods
if you wish to change the display values.


Callbacks
---------

To execute a callback function after a table column has been sorted, you can
bind on `aftertablesort`.

    var table = $("table").stupidtable();
    table.bind('aftertablesort', function (event, data) {
        // data.column - the index of the column sorted after a click
        // data.direction - the sorting direction (either asc or desc)
        // $(this) - this table object

        console.log("The sorting direction: " + data.direction);
        console.log("The column index: " + data.column);
    });

Similarly, to execute a callback before a table column has been sorted, you can
bind on `beforetablesort`.

See the complex_example.html file.

Creating your own data types
----------------------------

Sometimes you don't have control over the HTML produced by the backend. In the
event you need to sort complex data without a `data-sort-value` attribute, you
can create your own data type. Creating your own data type for sorting purposes
is easy as long as you are comfortable using custom functions for sorting.
Consult [Mozilla's Docs][1] if you're not.

Let's create an alphanum datatype for a User ID that takes strings in the form
"D10", "A40", and sorts the column based on the numbers in the string.

    <thead>
      <tr>
        <th data-sort="string">Name</th>
        <th data-sort="int">Age</th>
        <th data-sort="alphanum">UserID</th>
      </tr>
    </thead>
    <tbody>
      <tr>
        <td>Joseph McCullough</td>
        <td>20</td>
        <td>D10</td>
      </tr>
      <tr>
        <td>Justin Edwards</td>
        <td>29</td>
        <td>A40</td>
      </tr>
      ...
      ...
      ...

Now we need to specify how the **alphanum** type will be sorted. To do that,
we do the following:

    $("table").stupidtable({
      "alphanum":function(a,b){

        var pattern = "^[A-Z](\\d+)$";
        var re = new RegExp(pattern);

        var aNum = re.exec(a).slice(1);
        var bNum = re.exec(b).slice(1);

        return parseInt(aNum,10) - parseInt(bNum,10);
      }
    });

This extracts the integers from the cell and compares them in the style
that sort functions use.

License
-------

The Stupid jQuery Plugin is licensed under the MIT license. See the LICENSE
file for full details.

Tests
-----

Visit `tests/test.html` in your browser to run the QUnit tests.


[0]: http://joequery.github.io/Stupid-Table-Plugin/
[1]: https://developer.mozilla.org/en/JavaScript/Reference/Global_Objects/Array/sort
