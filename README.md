# babashka-pigpio

A command-line interface for controlling Raspberry Pi GPIO pins using `babashka` and the `pigpio` library. This tool communicates with the `pigpiod` daemon to perform GPIO operations.

## Prerequisites

Before using this tool, ensure you have the following installed and running:

*   [Babashka](https://babashka.org/): A native Clojure interpreter for scripting.
*   `pigpiod`: The `pigpio` daemon must be running on your Raspberry Pi. You can usually start it with `sudo pigpiod`.

## Usage

You can list all available tasks by running the following command in your terminal:

```bash
bb tasks
```

This will display a list of tasks and their descriptions.

### Available Tasks

Here are some of the available tasks and how to use them:

*   **`get-hardware-version`**: Gets the hardware version of the Raspberry Pi.
    ```bash
    bb get-hardware-version
    ```

*   **`set-mode <pin> <mode>`**: Sets a specific GPIO pin to input or output mode.
    ```bash
    bb set-mode 17 output
    ```

*   **`write-pin <pin> <level>`**: Writes a high (1) or low (0) value to a GPIO pin.
    ```bash
    # Write pin 17 high
    bb write-pin 17 1

    # Write pin 17 low
    bb write-pin 17 0
    ```

*   **`read-pin <pin>`**: Reads the current level of a GPIO pin.
    ```bash
    bb read-pin 17
    ```

*   **`get-mode <pin>`**: Gets the current mode of a GPIO pin.
    ```bash
    bb get-mode 17
    ```

## Development

The tasks are defined as functions in `src/tasks.clj` and are exposed through the `bb.edn` configuration file. Command-line argument parsing and validation are handled using `babashka.cli`.