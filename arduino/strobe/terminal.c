#include "terminal.h"

void terminal_init(terminal_t *term)
{
  // Initialize the state
  term->current_handler = NULL;
  term->handler_chain = NULL;
  term->character_index = 0;
  term->reset_index = 0;
}

void terminal_attach(terminal_t *term, terminal_cmd_t *cmd)
{
  // Attach the new one to the end of the linked list
  terminal_cmd_t *last;
  for(last = term->handler_chain; last != NULL; last = last->next);
  last->next = cmd;
}

void terminal_feed(terminal_t *term, char incoming)
{
  // Handle the reset mechanism
  if(incoming == RESET_CHARACTER) {
    term->reset_index++;
    if(term->reset_index == RESET_LENGTH) {
      term->current_handler = NULL;
      term->character_index = 0;
      return;
    }
  } else {
    term->reset_index = 0;
  }

  // If there is no handler, see if this triggers one
  if(term->current_handler == NULL) {
    terminal_cmd_t *cmd;
    for(cmd = term->handler_chain; cmd != NULL; cmd = cmd->next) {
      if(cmd->trigger == incoming) {
        term->current_handler = cmd;
        break;
      }
    }
  }

  // Handle the incoming data
  if(term->current_handler) {
    term->buffer[term->character_index] = incoming;
    term->character_index++;
    if(term->character_index > term->current_handler->length) {
      term->character_index = 0;
      term->current_handler = NULL;
      term->current_handler->handler((void *)term->buffer);
    }
  }
}
