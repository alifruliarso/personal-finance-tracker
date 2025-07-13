let alert_list = document.querySelectorAll(".alert");
alert_list.forEach(function (alert) {
  new bootstrap.Alert(alert);

  let alert_timeout = alert.getAttribute("data-timeout");
  setTimeout(() => {
    bootstrap.Alert.getInstance(alert).close();
  }, +alert_timeout);
});

window.addEventListener("DOMContentLoaded", () => {
  // console.log("Page loaded, setting active link.");

  const links = document.querySelectorAll(".nav-link");
  // console.log("Links found:", links);

  // sets active link based on the current URL path.
  const setActiveLink = () => {
    // console.log("Setting active link based on current URL path:", window.location.href);

    links.forEach((link) => {
      // console.log("Link href:", link.getAttribute("href"));
      // console.log("location href:", window.location.pathname);
      // checks if link's href matches the page's url
      if (link.getAttribute("href") === window.location.pathname) {
        // console.log("Link is active:", link);
        if (!link.classList.contains("active")) {
          console.log("Adding active class to link:", link);
          link.classList.add("active");
        }
        link.setAttribute("aria-current", "page");
      } else {
        // console.log("Link is no longer active:", link);
        link.classList.remove("active");
        link.removeAttribute("aria-current");
      }
    });
  };

  // calls setActiveLink on page load
  setActiveLink();

  //updates active state on click.
  links.forEach((link) => {
    link.addEventListener("click", () => {
      setActiveLink();
    });
  });
});

/**
 * Register an event at the document for the specified selector,
 * so events are still catched after DOM changes.
 */
function handleEvent(eventType, selector, handler) {
  document.addEventListener(eventType, function (event) {
    if (event.target.matches(selector + ", " + selector + " *")) {
      handler.apply(event.target.closest(selector), arguments);
    }
  });
}

handleEvent("submit", ".js-submit-confirm", function (event) {
  if (!confirm(this.getAttribute("data-confirm-message"))) {
    event.preventDefault();
    return false;
  }
  return true;
});

handleEvent("click", "body", function (event) {
  // close any open dropdown
  const $clickedDropdown = event.target.closest(".js-dropdown");
  const $dropdowns = document.querySelectorAll(".js-dropdown");
  $dropdowns.forEach(($dropdown) => {
    if (
      $clickedDropdown !== $dropdown &&
      $dropdown.getAttribute("data-dropdown-keepopen") !== "true"
    ) {
      $dropdown.ariaExpanded = "false";
      $dropdown.nextElementSibling.classList.add("hidden");
    }
  });
  // toggle selected if applicable
  if ($clickedDropdown) {
    $clickedDropdown.ariaExpanded =
      "" + ($clickedDropdown.ariaExpanded !== "true");
    $clickedDropdown.nextElementSibling.classList.toggle("hidden");
    event.preventDefault();
  }
});

function initDatepicker() {
  document
    .querySelectorAll(".js-datepicker, .js-timepicker, .js-datetimepicker")
    .forEach(($item) => {
      const flatpickrConfig = {
        allowInput: true,
        time_24hr: true,
        enableSeconds: true,
      };
      if ($item.classList.contains("js-datepicker")) {
        flatpickrConfig.dateFormat = "Y-m-d";
      } else if ($item.classList.contains("js-timepicker")) {
        flatpickrConfig.enableTime = true;
        flatpickrConfig.noCalendar = true;
        flatpickrConfig.dateFormat = "H:i:S";
      } else {
        // datetimepicker
        flatpickrConfig.enableTime = true;
        flatpickrConfig.altInput = true;
        flatpickrConfig.altFormat = "Y-m-d H:i:S";
        flatpickrConfig.dateFormat = "Y-m-dTH:i:S";
        // workaround label issue
        flatpickrConfig.onReady = function () {
          const id = this.input.id;
          this.input.id = null;
          this.altInput.id = id;
        };
      }
      flatpickr($item, flatpickrConfig);
    });
}
initDatepicker();
