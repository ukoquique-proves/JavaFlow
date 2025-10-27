# 🚀 Deploying with Podman on PuppyLinux

This guide documents the steps, challenges, and solutions for running the JavaFlow ecosystem on PuppyLinux, a non-standard environment for containerization. Due to Docker compatibility issues, we opted for Podman, a lightweight, daemonless container engine.

---

## 1. The Initial Challenge: Docker on PuppyLinux

Our first attempts to use the standard `docker-compose.yml` failed because the Docker daemon was not running or installable on the minimal PuppyLinux distribution.

- **Error**: `Cannot connect to the Docker daemon at unix:///var/run/docker.sock.`

This required finding an alternative containerization strategy.

---

## 2. Solution: Adopting Podman

We chose **Podman** as the best alternative for the following reasons:
- **Daemonless**: It doesn't require a background service, making it more suitable for lightweight systems.
- **Docker-Compatible**: It uses the same command-line interface (CLI) as Docker.
- **Availability**: It was available in the PuppyLinux (Debian-based) package repositories.

### Installation Steps

We installed the following packages using `apt` (via the Synaptic Package Manager):

1.  `podman`: The core container engine.
2.  `podman-compose`: A script to run `docker-compose.yml` files using Podman.
3.  `podman-docker`: A compatibility package that allows using the `docker` command, which gets redirected to `podman`.

---

## 3. Configuration & Troubleshooting

Getting Podman to run on PuppyLinux required solving several environment-specific issues.

### Issue A: Storage Driver Incompatibility

- **Error**: `Error: 'overlay' is not supported over overlayfs`
- **Cause**: PuppyLinux's underlying filesystem does not support Podman's default `overlay` storage driver.
- **Solution**: We configured Podman to use the `vfs` (Virtual File System) driver, which is slower and uses more disk space but is universally compatible.

**Configuration (`~/.config/containers/storage.conf`):**
```ini
[storage]
driver = "vfs"
```

### Issue B: Unqualified Image Names

- **Error**: `short-name ... did not resolve to an alias and no unqualified-search registries are defined`
- **Cause**: Podman, by default, doesn't know where to pull images like `postgres:15-alpine`. It needs to be told to search `docker.io`.
- **Solution**: We created a registries configuration file.

**Configuration (`/etc/containers/registries.conf`):**
```ini
unqualified-search-registries = ["docker.io"]
```

### Issue C: Disk Space Exhaustion

- **Error**: `no space left on device`
- **Cause**: The `vfs` storage driver, combined with the large size of the Maven build context, exhausted the available disk space on the root partition.
- **Solution**: We identified and deleted several large, unused `node_modules` directories from other projects on the system to free up gigabytes of space.

### Issue D: Persistent Port and Lock Conflicts

- **Error**: `address already in use` and `deadlock due to lock mismatch`
- **Cause**: After system restarts or failed `podman-compose` attempts, lingering network namespaces and container locks remained, preventing new containers from starting.
- **Solution**:
    1.  **System Restart**: The most reliable way to clear all locks and ports.
    2.  **Podman System Reset**: The `podman system reset --force` command was used to completely wipe all containers, volumes, and network configurations, providing a clean slate.
    3.  **Port Change**: We changed the host port for PostgreSQL from `5432` to `5433` to avoid any potential conflicts with local services.

---

## 4. The Final Hybrid Solution

Due to the disk space constraints of building the Java application inside a container on this system, we adopted a hybrid approach that provides the full development environment:

1.  **Run Services in Podman**: A minimal `docker-compose-minimal.yml` file runs only the essential services (PostgreSQL and Flowable Modeler) in containers.
2.  **Run JavaFlow Locally**: The main JavaFlow application is run directly on the host using the `./run.sh` script.

### Final Workflow

1.  **Start Containerized Services**:
    ```bash
    # Ensure no old containers are running
    podman system reset --force

    # Start postgres and flowable-modeler
    podman-compose -f docker-compose-minimal.yml up -d
    ```

2.  **Start the Java Application**:
    ```bash
    # This will connect to the PostgreSQL container
    ./run.sh
    ```

This pragmatic solution delivers the complete, functional ecosystem while respecting the resource limitations of the PuppyLinux environment.
