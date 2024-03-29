package routes;

import exceptions.EmptyRouteException;
import exceptions.IncompatibleTypeException;
import exceptions.TransportFormatException;
import stops.Stop;
import utilities.Writeable;
import vehicles.PublicTransport;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a route in the transportation network.
 *
 * <p>A route is essentially a collection of stops which public transport
 * vehicles can follow.
 */
public abstract class Route implements Writeable {
    // the name of the route
    private String name;

    // the number of the route
    private int routeNumber;

    // tracks where vehicles are currently located on the route
    private List<PublicTransport> vehicles;

    // the stops which make up the route
    private List<Stop> route;

    /**
     * Creates a new Route with the given name and number.
     *
     * <p>The route should initially have no stops or vehicles on it.
     *
     * <p>If the given name contains any newline characters ('\n')
     * or carriage returns ('\r'), they should be removed from the
     * string before it is stored.
     *
     * <p>If the given name is null, an empty string should be stored in its place.
     *
     * @param name The name of the route.
     * @param routeNumber The route number of the route.
     */
    public Route(String name, int routeNumber) {
        this.name = name == null ? "" : name.replace("\n", "").replace("\r", "");
        this.routeNumber = routeNumber;
        this.vehicles = new ArrayList<>();
        this.route = new ArrayList<>();
    }

    /**
     * Returns the name of the route.
     *
     * @return The route name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the number of the route.
     *
     * @return The route number.
     */
    public int getRouteNumber() {
        return routeNumber;
    }

    /**
     * Returns the stops which comprise this route.
     *
     * <p>The order of the stops in the returned list should be the same as the
     * order in which the stops were added to the route.
     *
     * <p>Modifying the returned list should not result in changes to the internal
     * state of the class.
     *
     * @return The stops making up the route.
     */
    public List<Stop> getStopsOnRoute() {
        return new ArrayList<>(route);
    }

    /**
     * Returns the first stop of the route (i.e. the first stop to be added to the
     * route).
     *
     * @return The start stop of the route.
     * @throws EmptyRouteException If there are no stops currently on the route
     */
    public Stop getStartStop() throws EmptyRouteException {
        if (route.isEmpty()) {
            throw new EmptyRouteException();
        }

        return route.get(0);
    }

    /**
     * Adds a stop to the route.
     *
     * <p>If the given stop is null, it should not be added to the route.
     *
     * <p>If this is the first stop to be added to the route, the given stop should
     * be recorded as the starting stop of the route. Otherwise, the given stop
     * should be recorded as a neighbouring stop of the previous stop on the route
     * (and vice versa) using the {@link Stop#addNeighbouringStop(Stop)} method.
     *
     * <p>This route should also be added as a route of the given stop (if the given
     * stop is not null) using the {@link Stop#addRoute(Route)} method.
     *
     * @param stop The stop to be added to this route.
     */
    public void addStop(Stop stop) {
        if (stop == null) {
            return;
        }

        stop.addRoute(this);
        route.add(stop);

        // return if this was the first stop
        if (route.size() == 1) {
            return;
        }

        Stop previous = route.get(route.size() - 2);
        previous.addNeighbouringStop(stop);
        stop.addNeighbouringStop(previous);
    }

    /**
     * Returns the public transport vehicles currently on this route.
     *
     * <p>No specific order is required for the public transport objects in the
     * returned list.
     *
     * <p>Modifying the returned list should not result in changes to the internal
     * state of the class.
     *
     * @return The vehicles currently on the route.
     */
    public List<PublicTransport> getTransports() {
        return new ArrayList<>(this.vehicles);
    }

    /**
     * Adds a vehicle to this route.
     *
     * <p>If the given transport is null, it should not be added to the route.
     *
     * <p>The method should check for the transport being null first, then for an
     * empty route, and then for incompatible types (in that order).</p>
     *
     * @param transport The vehicle to be added to the route.
     * @throws EmptyRouteException If there are not yet any stops on the route.
     * @throws IncompatibleTypeException If the given vehicle is of the incorrect
     *      type for this route. This depends on the type of the route, i.e. a
     *      BusRoute can only accept Bus instances.
     */
    public void addTransport(PublicTransport transport)
            throws EmptyRouteException, IncompatibleTypeException {
        if (transport == null) {
            return;
        }

        if (route.isEmpty()) {
            throw new EmptyRouteException();
        }

        if (!getType().equals(transport.getType())) {
            throw new IncompatibleTypeException();
        }

        vehicles.add(transport);
    }

    /**
     * Compares this stop with another object for equality.
     *
     * Two routes are equal if their names and route numbers are equal.
     *
     * {@inheritDoc}
     *
     * @param other The other object to compare for equality.
     * @return True if the objects are equal (as defined above), false otherwise
     * (including if other is null or not an instance of the {@link Route}
     * class.
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Route)) {
            return false;
        }
        Route otherRoute = (Route) other;
        return name.equals(((Route) other).getName())
                && routeNumber == otherRoute.getRouteNumber();
    }

    @Override
    public int hashCode() {
        return routeNumber;
    }

    /**
     * Returns the type of this route.
     *
     * @return The type of the route (see subclasses)
     */
    public abstract String getType();

    /**
     * Creates a string representation of a route in the format:
     *
     * <p>'{type},{name},{number}:{stop0}|{stop1}|...|{stopN}'
     *
     * <p>without the surrounding quotes, and where {type} is replaced by the
     * type of the route, {name} is replaced by the name of the route, {number}
     * is replaced by the route number, and {stop0}|{stop1}|...|{stopN} is replaced by a
     * list of
     * the names of the stops stops making up the route. For example:
     *
     * <p>bus,red,1:UQ Lakes|City|Valley
     *
     * @return A string representation of the route.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(getType()).append(",");
        builder.append(name).append(",").append(routeNumber);
        builder.append(":");

        for (Stop stop : route) {
            builder.append(stop.getName()).append("|");
        }

        if (!route.isEmpty()) {
            builder.deleteCharAt(builder.length() - 1);
        }

        return builder.toString();
    }

    /**
     * Encodes this route as a string in the same format as specified in
     * toString().
     *
     * @return This route encoded as a string.
     */
    public String encode() {
        return toString();
    }

    /**
     * Creates a new route object based on the given string representation.
     *
     * @param routeString The string to decode, given in format
     *                    '{type},{name},{number}:{stop0}|{stop1}|...|{stopN}'.
     * @param existingStops The stops which currently exist in the transport
     *                      network.
     * @return The decoded route object (a BusRoute, TrainRoute, or FerryRoute,
     * depending on the type given in the string).
     * @throws TransportFormatException If the given string or existingStops
     * list is null, or the string is incorrectly formatted (according to the
     * encode() representation).
     */
    public static Route decode(String routeString,
                               List<Stop> existingStops)
            throws TransportFormatException {

        // neither the routeString nor existingStops should be null
        if ((routeString == null) || (existingStops == null)
                || (routeString.endsWith("|"))) {
            throw new TransportFormatException();
        }

        // ensure there are the correct amount of delimiters in the string
        countStaticDelimiters(routeString);
        String[] routeInfo = routeString.split(",", 3);

        // attempt to convert the routeNumber string to an integer
        int routeNumber = stringToInt(routeInfo[2].split(":")[0]);

        // create a new instance of the appropriate routeType
        Route route = newRouteInstance(routeInfo, routeNumber);

        // check if there are stops needed to be decoded
        checkStops(routeInfo, existingStops, route);

        return route;
    }

    /**
     * A private method to count that there are the correct number of static
     * Delimiters in the given string (ie. the delimiters which the number of
     * will always stay constant).
     *
     * @param routeString the string to compare the number of delimiters of.
     * @throws TransportFormatException if the incorrect number of delimiters
     * appear (and thus is in the wrong format).
     */
    private static void countStaticDelimiters(String routeString)
            throws TransportFormatException {

        // to find number of delimiters, we remove them from the string
        // and compare the difference in length to that of the original length
        int countCommas = routeString.length() -
                routeString.replaceAll(",","").length();
        int countSemicolons = routeString.length() -
                routeString.replaceAll(":","").length();

        if ((countCommas != 2) || (countSemicolons != 1)) {
            throw new TransportFormatException();
        }
    }

    /**
     * Attempts to parse the given string as an int.
     *
     * @param routeNumber the routeNumber in string format.
     * @return the routeNumber as an integer.
     * @throws TransportFormatException if the given string cannot
     * be parsed as an int.
     */
    private static int stringToInt(String routeNumber)
            throws TransportFormatException {
        try {
            return Integer.parseInt(routeNumber.trim());
        } catch (NumberFormatException e) {
            throw new TransportFormatException();
        }
    }

    /**
     * Create a new instance of the appropriate route type according
     * to the given string.
     *
     * @param routeInfo The given data used which will be used to create
     *        a route.
     * @return An instance of the appropriate route.
     */
    private static Route newRouteInstance(String[] routeInfo, int routeNumber)
            throws TransportFormatException {
        switch (routeInfo[0]) {
            case "bus" :
                return new BusRoute(routeInfo[1], routeNumber);
            case "ferry" :
                return new FerryRoute(routeInfo[1], routeNumber);
            case "train" :
                return new TrainRoute(routeInfo[1], routeNumber);
            default :
                throw new TransportFormatException();
        }
    }

    /**
     * Check if there are any stops in the string to be added to the route.
     *
     * @param routeInfo the array of strings holding the data to represent
     *                  the route.
     * @param existingStops a list of instances of existing stops.
     * @param route the route which is being decoded.
     * @throws TransportFormatException due to calling compareAndAddStops
     * which may throw an exception.
     */
    private static void checkStops(String[] routeInfo,
                                   List<Stop> existingStops, Route route)
            throws TransportFormatException {

        // check if there are any stops in the routeString,
        // and if so create an array of strings of them
        if (routeInfo[2].split(":").length > 1) {
            String[] stopStrings =
                    routeInfo[2].split(":")[1].split("\\|");
            compareAndAddStops(stopStrings, existingStops, route);
        }
    }

    /**
     * Compare the given array of strings with the existing stop, and add the
     * matching stops to the route.
     *
     * @param stopStrings the array of strings representing stops.
     * @param existingStops the list of existing stops known to the route.
     * @param route the route which the matching stops will be added to.
     * @throws TransportFormatException if a Stop string cannot be matched
     * with a stop.
     */
    private static void compareAndAddStops(String[] stopStrings,
                                           List<Stop> existingStops,
                                           Route route)
            throws TransportFormatException {
        for (String i : stopStrings) {
            for (Stop j : existingStops) {
                if (j.getName().equals(i)) {
                    route.addStop(j);
                    break;
                }

                // if the given string does not match any existing stops
                // there is an error in formatting
                if (existingStops.get(existingStops.size() - 1).equals(j)) {
                    throw new TransportFormatException();
                }
            }
        }
    }
}
