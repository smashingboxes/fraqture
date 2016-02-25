#include "terminal.h"

void terminal_init(terminal_t *term)
{
  // Initialize the state
  term->current_handler = NULL;
  term->handler_chain = NULL;
  term->character_index = 0;
  term->last_receive = 0;
}

void terminal_attach(terminal_t *term, terminal_cmd_t *cmd)
{
  // If there are no commands, start the chain.
  if(term->handler_chain == NULL) {
    term->handler_chain = cmd;
    return;
  }

  // Otherwise traverse the chain and add it to the end
  terminal_cmd_t *last;
  for(last = term->handler_chain; last->next != NULL; last = last->next);
  last->next = cmd;
}

void terminal_feed(terminal_t *term, char incoming, uint32_t millis)
{
  // Handle the reset mechanism
  if((millis - term->last_receive) > RESET_TIMEOUT) {
    term->current_handler = NULL;
    term->character_index = 0;
  }
  term->last_receive = millis;

  // Handle the incoming data
  if(term->current_handler) {
    term->buffer[term->character_index] = incoming;
    term->character_index++;
  }

  // If there is no handler, see if this triggers one
  if(!term->current_handler && term->handler_chain) {
    terminal_cmd_t *cmd;
    for(cmd = term->handler_chain; cmd != NULL; cmd = cmd->next) {
      if(cmd->trigger == incoming) {
        term->current_handler = cmd;
        break;
      }
    }
  }

  // If we have all the characters, trigger the handler.
  if(term->character_index == term->current_handler->length) {
    term->current_handler->handler((void *)term->buffer);
    term->character_index = 0;
    term->current_handler = NULL;
  }
}
